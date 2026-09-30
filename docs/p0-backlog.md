# 上线 P0 缺口 Backlog（待实施清单）

> 来源：ChatGPT 对 `business-review.md` 的审查报告（`product-launch-requirements.md`）。
> 状态：**已核实、暂缓，待另立项目实施**。本文档逐条给出代码证据、风险与建议方案。
> 本次已修复项：统一响应码为 200（见文末「已关闭项」）。
> 重要前提：`product-launch-requirements.md` 中的「管理端成功码不兼容」一条经核实为**误判**，详见文末。

---

## 1. 后台没有角色授权（RBAC）

- **严重性**：P0（上线阻塞）
- **代码证据**：
  - `security/UserSecurityInterceptor.java:83`：`/api/admin/**` 仅校验 `channel == "admin"`，**无 `role` / `isAdmin` 校验**。
  - `AdminUserController.java`（`/api/admin/users`）：提供 `GET/POST/PUT/DELETE` + `/{id}/reset-password`，任何 `channel=admin` 的 token 均可调用。
  - `AdminMarketController.java`（`/api/admin/market`）：提供 `POST/PUT/DELETE/{id}/audit` 等管理操作。
  - `JwtUtil` 签发时虽带 `role` claim，但拦截器从未读取使用。
- **风险**：普通账号（`channel=mini` 被拦）虽无法访问 admin，但**任何被标记 admin channel 的账号**（或后台注册的 admin）可管理用户、审核和删除信息；无「审核员」最小权限概念。
- **建议方案**：
  - 引入角色枚举：`ADMIN`（平台超管）、`COMMUNITY_ADMIN`（社区管理员）、`AUDITOR`（审核员）、`USER`。
  - 拦截器按 URL 前缀 + 角色做 RBAC：`/api/admin/**` 需 ADMIN 或对应社区角色；审核操作需 AUDITOR。
  - Service 层根据 JWT 中社区范围过滤，不信任客户端传入的 userId/communityId。
  - 涉及文件：`UserSecurityInterceptor`、新增角色/权限注解或配置、`User` 实体角色字段规范化。

---

## 2. 密码明文与固定默认密码

- **严重性**：P0（上线阻塞）
- **代码证据**：
  - `web/validator/user/UserLoginValidator.java:26-46`：`userService.list()` 遍历全量用户，`password.equals(...)` **明文比较**。
  - `service/UserService.java`：`miniLogin` 建号时 `user.setPassword("123456")`（明文）；`add`/`resetPassword` 默认 `"123456"`。
  - `UserMapper.xml`：`insert`/`update` 直接存 `password` 字段，无哈希。
- **风险**：账号接管、撞库、数据库泄露即泄露全部密码。
- **建议方案**：
  - 使用 BCrypt（`spring-security-crypto`）或 Argon2 哈希；存量密码迁移（首次登录强制改密）。
  - 登录限流 + 失败锁定 + 审计日志。
  - 涉及文件：`UserLoginValidator`、`UserService`、`pom.xml`（加依赖）、新增密码迁移工具。

---

## 3. 数据库基线不可信（缺列/无迁移）

- **严重性**：P0（上线阻塞）
- **代码证据**：
  - `park-admin/docs/database-schema.md` 的 `user` 表**只有** `id/name/doorplate/account/password/mobile/gmt_create/gmt_modify/gmt_active/avatar/description`，**缺 `openid/unionid/is_admin/channel/role`**。
  - 但 `UserMapper.xml` 的 `resultMap`、`insert`、`update` 均引用上述 5 列 → 新环境按 schema.md 建库后，小程序登录建号会因缺列直接失败。
  - 同样 `market` 表 schema.md 缺 `audit_remark/buildings`，但 `MarketMapper.xml` 已使用。
  - ⚠️ **补充核实**：根目录 `park.sql` 的 `user` 表**已含** `openid/unionid/is_admin/channel/role` 且带 `uk_openid` 唯一键（`park.sql:101-115`）——**真正的基线错误源是 `park-admin/docs/database-schema.md` 文档过时**，`park.sql` 是正确的。实施时应以 `park.sql` 为准，修订 `database-schema.md` 或直接废弃改用 `park.sql`。
- **风险**：新环境/发布时建库失败或功能缺失。
- **建议方案**：
  - 引入 Flyway/Liquibase 版本化迁移，从空库可完整执行。
  - 迁移内容至少含：`user` 补 `openid/unionid/is_admin/channel/role`；`market` 补 `community_id/status/audit_remark/audited_by/audited_at/withdrawn_at/closed_at/version`。
  - 涉及文件：`park.sql`、`park-admin/docs/database-schema.md`、新增迁移脚本目录。

---

## 4. 撤回/删除为物理删除（无审计）

- **严重性**：P0（上线阻塞）
- **代码证据**：`service/MarketService.java:235-253`：`delete(id)` 先 `deleteImagesByMarketCode` 再 `delete(id)`，**物理删除主记录与图片关系**。
- **风险**：无审计、无法恢复，投诉/纠纷不能追溯。
- **建议方案**：
  - 改软删除/`WITHDRAWN` 状态；记录操作者、时间、原因。
  - 新增 `market_audit_log` 表记录状态流转（前后值、原因、操作者、时间、traceId）。
  - 涉及文件：`MarketService`、`MarketMapper.xml`、`market` 表结构、新增审计表。

---

## 5. 登录态仅在单进程内失效（内存黑名单）

- **严重性**：P0（多实例/重启时）
- **代码证据**：`service/AuthTokenService.java`：黑名单为进程内 `ConcurrentHashMap`（上轮为去 Redis 而改）。重启或部署多实例后，已登出/被踢 token 在自身 `exp`（最长 30 天）内可复用。
- **风险**：登出失效、强制下线不可靠、多实例负载均衡下失效状态不共享。
- **建议方案**：
  - 生产引入 Redis（或数据库）共享会话失效状态；或 JWT 加服务端会话版本号（用户级 `token_version` 列，每次登录/踢人递增，拦截器比对）。
  - 涉及文件：`AuthTokenService`、`UserSecurityInterceptor`、`User` 实体/表。
  - ⚠️ 注意：本项目曾因「无 Redis 环境」特意去 Redis 改为纯 JWT，如回退 Redis 需确认部署环境具备 Redis。

---

## 6. 附带整改项（P0/P1 边界）

- **上传接口**：`AdminUploadController`（`/api/admin/upload`）需限制 MIME 类型、扩展名、大小、图片数量。
- **对象存储**：COS 建议私有读 + 签名 URL（当前 `tencent.cos.*` 配置见 `application*.properties`）。
- **日志脱敏**：已核实当前无 token/openid/password 日志（`logback-spring.xml` 仅打 `%msg`），**保持**并纳入回归检查。
- **旧接口兼容层**：`/api/login`、`/api/wx-login`、`/api/oauth/*` 等旧路径在迁移完成前保留，加弃用监控 30 天无调用后再删。

---

## 7. 新增数据与接口契约（MVP 需求）

（来自 `product-launch-requirements.md` §3-4，属功能新增，非纯缺陷修复）

- 新增表：`community`、`user_community_role`、`market_audit_log`、`report`、`operation_log`、`login_attempt`。
- `market` 增加租户边界 `community_id` 与状态机字段、乐观锁 `version`。
- 统一错误码：401（未认证）/403（无权限）/409（状态/版本冲突）/422（参数不合法）。
- 禁止把 MyBatis `User`/`Market` 实体直接作为请求/响应对象，改用 DTO/VO。
- 小区车位状态机：`DRAFT → PENDING → PUBLISHED → OFFLINE`、`PENDING → REJECTED → DRAFT`、`→ WITHDRAWN`、`→ CLOSED`。

---

## 8. 非功能与上线门槛（P1）

- HTTPS + 密钥仅部署环境注入；生产 JWT 密钥校验。
- 接口限流、依赖漏洞扫描、文件安全校验、备份恢复演练。
- 隐私政策/用户协议/举报与客服入口；账号注销与数据删除流程。
- 健康检查、结构化日志、trace ID、监控（登录成功率/审核时长/发布转化/举报率/P95）。
- 小程序备案、域名 ICP + HTTPS、隐私保护指引声明（`getLocation`、相册上传、openid 等用途）。

---

## 建议实施顺序（来自审查报告 §9）

1. 写安全与契约回归测试，复现 P0 问题。
2. 密码哈希、RBAC、共享会话失效、DTO/VO、统一响应契约；删敏感日志。
3. 数据库迁移（Flyway），导入现有数据 + 社区/状态机/审计/软删除字段表。
4. Service 层实现社区范围、归属、状态流转、乐观锁；Controller 只收 DTO。
5. 对齐管理端和小程序 API，端到端回归；旧接口弃用监控后再删。
6. 运维配置、健康检查、备份恢复、监控与发布流水线；预发演练通过后提审。

> 任何 P0 未通过，禁止标记为生产可用。

---

## 已关闭项（本次已修复）

### A. 统一响应码为 200（审查报告 §2 第 3 条）

- **核实结论**：ChatGPT 所述「后端返回 200、管理端判 0、不兼容」为**误判**。实际：
  - 后端 `ApiResponse.success()` 默认 `code = 0`（非 200）。
  - 管理端实际使用的 `services/request.ts` 判 `code === 0`（一致）。
  - `lib/request/server.ts`/`client.ts` 判 `code === 200`，但**未被页面引用**（死代码）。
  - 文档 `mini-login.md` 写 `code:200`；小程序 `silentRelogin` 判 `body.code === 200`（当前后端返 0 会误判）。
- **已改动**（统一为 200，全链路对齐）：
  - `park-manger-java/.../model/meta/ApiResponse.java`：默认 `code = 0` → `200`，注释同步。
  - `park-admin/services/request.ts`：`code === 0` → `code === 200`。
  - `park-admin/app/(protected)/market/page.tsx`：`code === 0 || response.success` → `code === 200`。
  - `park-applet/server/request.js`：无需改（`successCode` 已含 200，`silentRelogin` 恢复正确）。
- **验证**：后端 `mvnw clean compile` ✅；小程序 `node --check` ✅；管理端 eslint 改动文件无新增错误 ✅。

### B. 敏感日志检查（审查报告 §2 末）

- 核实后端**无**任何 `token/openid/password/Authorization` 写入日志；`logback-spring.xml` 仅 `%msg`。该项**已满足**，无需改动，纳入回归。
