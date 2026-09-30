# 微信小程序快捷登录 - 业务实现文档（供代码审查）

> 文档目的：向审查者（ChatGPT）完整说明「原生微信小程序 + Java21 SpringBoot Maven 个人小程序快捷登录」
> 的当前业务实现、改动范围、设计决策与已知风险，便于逐项审查。
> 仓库结构：
> - 小程序前端：`park-applet/`（原生微信小程序）
> - 后端：`park-manger-java/`（Spring Boot 3.2.3 / Java 21 / MyBatis / Maven）
> 配套文档：`park-manger-java/docs/mini-login.md`（登录接口接入说明）

---

## 一、业务需求回顾（豆包提示词）

1. 小程序登录页：登录按钮 → `wx.login` 拿 code → POST 后端登录接口。
2. 后端收 code → 调微信 `jscode2session`（AppID+AppSecret+code）→ 拿 openid。
3. 查 `wx_user` 表：存在则发 JWT；不存在则自动建用户后发 JWT。
4. 小程序存 token 到本地，全局 request 带 `Authorization: Bearer <token>`；捕获 401 跳登录页。
5. 后端 JWT 工具类 + 全局 token 拦截器。

### 硬性约束
- ① 个人小程序，**禁止 getPhoneNumber / 手机号解密**。
- ② AppSecret 只放后端配置，前端绝不暴露。
- ③ `wx.login` code 一次性；异常捕获：微信接口失败 / code 过期 / 网络异常 / JWT 过期。
- ④ Java 21、Maven、依赖适配 Java21。
- ⑤ 开发：开发者工具不校验域名；上线：后端必须 HTTPS + 小程序配置合法域名。

### 用户决策（实施时确认）
- 用户表：**复用现有 `user` 表**（含 `openid` 字段），不新建 `wx_user`。
- Redis：**无 Redis**，改为纯 JWT + 内存黑名单方案。
- 范围：方案 A-E 全部执行。

---

## 二、现状盘点（改动前）

### 后端已具备
| 能力 | 位置 |
|---|---|
| 登录接口（白名单放行） | `web/wechat/WechatAuthController#mini-login` → `POST /api/wechat/auth/mini-login` |
| jscode2session 换 openid | `service/UserService#resolveWechatOpenid` |
| 查/建 user | `service/UserService#miniLogin` |
| 签发 JWT（mini channel） | `util/JwtUtil#generateMiniToken`、`service/AuthTokenService#issueMiniToken` |
| token 拦截器 | `security/UserSecurityInterceptor` + `security/InterceptorConfig` |
| 自动续期（响应头 X-New-Token） | `UserSecurityInterceptor#preHandle` |
| JWT 依赖 jjwt 0.12.6 | `pom.xml` |
| Secret 走环境变量 | `WECHAT_MINIAPP_APP_ID` / `WECHAT_MINIAPP_SECRET` |

### 小程序端已具备
| 能力 | 位置 |
|---|---|
| 登录页（按钮 + wx.login） | `pages/login/index.*`（TDesign） |
| 全局 request 封装 | `server/request.js`（`_post/_get/_put/_delete/_upload`） |
| 启动静默登录 | `app.js#silentLogin` |
| HTTPS baseURL | `config/net.config.js` / `server/url.js` |

---

## 三、本次改动清单

### 后端 `park-manger-java`（10 个文件）
| 文件 | 改动 | 目的 |
|---|---|---|
| `service/AuthTokenService.java` | **重写**：删除 Redis 依赖，改为纯 JWT + 内存 `ConcurrentHashMap` 黑名单（`jti→过期时间`）+ `activeJtiByUser`（用户+channel→jti） | 去 Redis 后保持「黑名单/单点互踢/强制下线」语义 |
| `config/RedisConfig.java` | **删除** | 去 Redis |
| `security/UserSecurityInterceptor.java` | 移除 `isCurrentToken` 单点校验调用，保留黑名单校验 + 自动续期 | 适配无 Redis |
| `pom.xml` | 移除 `spring-boot-starter-data-redis`、`commons-pool2` | 去 Redis |
| `application.properties` / `application-dev.properties` / `application-prod.properties` | 删除 `spring.data.redis.*` 配置段 | 去 Redis |
| `model/request/LoginForm.java` | 删除 `mobile` 字段 | 约束① 去手机号 |
| `service/UserService.java` | `miniLogin` 去除按手机号回查/回填逻辑；`resolveWechatOpenid` 异常细分 | 约束① + 约束③ |
| `docs/mini-login.md` | 错误码表加 `WECHAT_NETWORK_ERROR`；Token 策略改为内存黑名单描述；请求体去掉 `mobile` | 文档同步 |

### 小程序端 `park-applet`（2 个文件）
| 文件 | 改动 | 目的 |
|---|---|---|
| `pages/login/index.js` | 删除 `getPhoneNumber` 方法 | 约束① |
| `server/request.js` | 401/`UNAUTHENTICATED` → 静默重登重试 1 次 → 失败跳登录页；消费 `X-New-Token` 自动续期；修复重试时请求体被响应体覆盖的 bug；`_upload` 401 清 token 跳登录页 | 业务需求 4 |

### 新增文件
| 文件 | 说明 |
|---|---|
| `docs/business-review.md` | 本文档 |
| `.opencode/plans/plan-wx-mini-login-no-redis.md` | 实施规划留档 |

---

## 四、登录时序（当前实现）

```
小程序                                  后端 (park-manger-java)                    微信服务器
  │─ wx.login() ──────────────────────► 拿到 code
  │◄─ code ───────────────────────────
  │─ POST /api/wechat/auth/mini-login {code, token?} ─►
  │                                      │─ GET sns/jscode2session (appid/secret/code) ─►
  │                                      │◄─ {openid, session_key, ...} ────────────────
  │                                      │  查 user by openid → 无则建（account=wx_<openid>）
  │                                      │  revokeIfPresent(旧token) → 旧 jti 拉黑
  │                                      │  签发新 JWT（channel=mini, jti, exp=30天）
  │◄─ {code:200, data:{id, nickname, token, ...}} ──
  │  本地存 token / userInfo
  │─ 后续请求 header: Authorization: Bearer <token> ─►
  │                                      │  UserSecurityInterceptor: 验签+过期+黑名单+channel 前缀
  │                                      │  剩余<7天时下发 X-New-Token 自动续期
```

---

## 五、后端核心实现说明

### 5.1 纯 JWT 认证（无 Redis）— `AuthTokenService`
- `jtiBlacklist`：`ConcurrentHashMap<String, Long>`（jti → 过期毫秒时间戳）。`isBlacklisted` 惰性清理过期项。
- `activeJtiByUser`：`ConcurrentHashMap<String, String>`（`auth:token:mini:<userId>` → jti）。
- `issueMiniToken`：生成新 jti → 记录 activeJtiByUser → 签发 JWT。若该用户已有旧 jti，按 mini TTL（30 天）拉黑旧 jti。
- `issueAdminToken`：后台 token，TTL 86400s，同机制。
- `revokeJti(jti, remainingSeconds)`：登出/被顶时按剩余时长拉黑。
- `revokeIfPresent(oldToken)`：登录带旧 token 时拉黑旧 jti。
- `forceLogout(userId, channel, fallbackTtl)`：从 activeJtiByUser 取当前 jti 拉黑。
- `purgeExpired()`：清理过期黑名单（当前无调用方，公共方法）。

**风险**：内存黑名单重启即清空。已登出/被顶 token 在自身 `exp` 到期（最长 30 天）前重启后可复用。个人小程序可接受；如需强一致需引入 Redis/DB。

### 5.2 登录服务 — `UserService#miniLogin`
```
openid = resolveWechatOpenid(code)          // 调微信换 openid（异常细分）
user = getUserByOpenid(openid)               // 已有 openid 用户
if null: 按 account='wx_'+openid 查旧用户 → 命中则补 openid + channel=mini
if null: 新建（name 默认"微信用户", password 默认"123456", isAdmin=0, channel=mini, role=user）
revokeIfPresent(loginForm.token)             // 带旧 token 则拉黑
token = issueMiniToken(user)
return SimpleUser(user, token)
```

### 5.3 异常细分 — `UserService#resolveWechatOpenid`
| 场景 | 错误码 | 说明 |
|---|---|---|
| AppID/Secret 未配置 | `WECHAT_CONFIG_ERROR` | 空值即抛 |
| 微信 HTTP 调用异常 / 无响应 | `WECHAT_NETWORK_ERROR` | RestTemplate 异常 / 空响应 |
| 微信返回 errcode≠0（40029 code 无效、40163 code 已使用 等） | `WECHAT_LOGIN_ERROR` | 附 `(errcode): errmsg` |
| 响应解析失败 / openid 为空 | `WECHAT_LOGIN_ERROR` | 兜底 |

### 5.4 JWT — `JwtUtil`
- 密钥 `jwt.secret`（HMAC，≥32 字节），从 properties 注入，prod 用 `${JWT_SECRET}`。
- mini TTL `jwt.mini.ttl` = 2592000s（30 天）；续期阈值 `jwt.mini.refresh-threshold` = 604800s（7 天）。
- Payload：`sub`=userId、`id`=jti、`channel`=mini/admin、`openid`、`role`、`iat`、`exp`。

### 5.5 拦截器 — `UserSecurityInterceptor`
1. 取 `Authorization`（支持 `Bearer ` 前缀或裸 token）。
2. 空 → 401。
3. `parseClaims` 验签 + 过期 → 401。
4. 黑名单 `isBlacklisted(jti)` → 401「登录已失效」。
5. channel 前缀校验：`/api/wechat/**` 仅 mini；`/api/admin/**` 仅 admin → 401。
6. 写入 request attribute（userId/channel/openid）。
7. mini 通道剩余时长 < 7 天 → 签发新 token，`revokeJti` 旧 jti，响应头 `X-New-Token`。

白名单（`InterceptorConfig`）：`/api/wechat/auth/mini-login`、`/api/admin/auth/login`、`/api/wechat/market/list`（历史遗留）、`/api/login`、`/api/wx-login`、`/api/oauth/mini/login`、`/api/register`、`/favicon.ico`、`/lib/**`、`/api/sso/**`、swagger 相关。

### 5.6 统一响应
- 成功：`ApiResponse`（`code:200`、`success:true`、`data`、`message`）。
- 401 拦截器：`ApiErrorResponse`（HTTP 401 + `error: UNAUTHENTICATED` + `message` + `path`）。
- 业务异常：`ExceptionHandler`（`@ControllerAdvice`）→ `ApiErrorResponse`（`error`=错误码如 `WECHAT_LOGIN_ERROR`）。

---

## 六、小程序端核心实现说明

### 6.1 登录页 `pages/login/index.js`
- 勾选协议 → `wx.login` 拿 code → `wxLogin({code})`（`POST /api/wechat/auth/mini-login`）→ 成功存 token/userInfo → `wx.switchTab('/pages/home/index')`。
- 失败展示 errMsg；无 code / wx.login fail 也兜底提示。
- 已删除 `getPhoneNumber`。

### 6.2 全局请求 `server/request.js`
- `request({url, method, data, header, retried})`：
  - 自动带 `Authorization: Bearer <token>` + `token` + `dataType` 头。
  - 响应成功且 `code ∈ successCode([0,'0',200])` → 检查 `X-New-Token` 响应头 → 更新本地 token → resolve。
  - **401 分支**：清 token/userInfo → 若 `!retried` → `silentRelogin()`（`wx.login` 换 code → mini-login 换 token）→ 成功则用原始请求体重试 1 次（`retried=true`）→ 仍 401 或重登失败则跳登录页。
  - 其他错误：toast message，resolve。
- `silentRelogin()`：返回 Promise<boolean>，仅调白名单接口，不会递归 401。
- `_upload`：401 清 token 后直接跳登录页，**不做上传重试**（避免重复上传副作用）。
- **修复的 bug**：原 `success({data})` 解构使 `data` 在回调内变为响应体，重试时误把响应体当请求体；已用 `requestData` 快照修复。

### 6.3 地址配置
- `server/url.js`：`develop: 'http://127.0.0.1:3429'`、`trial: 'http://192.168.188.110:702'`、`release: 'https://wx.greandata1.com'`。
- `getUrlByEnv.js`：按 `wx.getAccountInfoSync().miniProgram.envVersion` 选地址。
- 注意：`config/net.config.js` 的 `baseURL` 实际未被 request.js 使用（request.js 用 `getUrl()`），两处地址需保持一致意识。

---

## 七、验证结果

| 项 | 结果 |
|---|---|
| 后端编译 `mvnw clean package -DskipTests`（IDEA JBR JDK21） | ✅ BUILD SUCCESS（68 源文件，Java 21） |
| 小程序 JS 语法 `node --check`（request.js / login/index.js / app.js） | ✅ 全部通过 |
| 括号平衡校验（6 个改动文件） | ✅ 全部 OK |
| grep 复查 | ✅ 无 `isCurrentToken`、无 Redis 代码残留（仅注释提及）、无 `getPhoneNumber` 残留 |
| 后端运行时启动 | ❌ **被数据库阻断**（详见第八节） |

---

## 八、当前阻塞项（审查时重点关注的未决事项）

1. **数据库不可达**：dev 库 `mysql-internet-cn-north-1-336723bd277f4c12.rds.jdcloud.com:3306`（116.196.70.97）端口连接超时。
   京东云 RDS 可能未对当前 Mac 的公网 IP 开放白名单，或该实例已不可用。**需用户确认可连的 DB 地址/账号**，改 `application-dev.properties` 后启动联调。
2. **AppID/AppSecret 未提供**：登录接口在无这两个环境变量时必然返回 `WECHAT_CONFIG_ERROR`。需用户提供后注入启动。
3. **`user` 表结构未核实**：DB 可达前无法确认 `openid`/`channel` 字段是否存在（登录建号依赖）。如缺字段需补 ALTER。

---

## 九、硬性约束满足情况核对

| 约束 | 状态 | 说明 |
|---|---|---|
| ① 禁手机号 | ✅ | 前端删除 `getPhoneNumber`；`LoginForm` 删 `mobile`；`UserService` 删手机号回查/回填。保留项：`User` 实体、`user` 表 `mobile` 列、`SimpleUser.mobile`、`WechatUserController` 更新资料可带 mobile——均为**普通资料字段**，非 getPhoneNumber 解密逻辑，与约束不冲突 |
| ② Secret 不暴露前端 | ✅ | 只走后端环境变量 `WECHAT_MINIAPP_APP_ID/SECRET`，properties 不写占位（避免循环引用），前端无任何 AppSecret |
| ③ 异常捕获 | ✅ | `resolveWechatOpenid` 细分 微信接口失败/网络异常/code 无效过期/无 openid；JWT 过期由拦截器捕获返回 401；code 一次性由微信侧保证（40029/40163 映射到 `WECHAT_LOGIN_ERROR`） |
| ④ Java21 + Maven | ✅ | `pom.xml` java.version=21，Spring Boot 3.2.3，jjwt 0.12.6 适配 Java21；已编译通过 |
| ⑤ HTTPS + 合法域名 | ⚠️ 待上线 | dev 用 `http://127.0.0.1`（开发者工具勾选不校验域名）；release 域名 `https://wx.greandata1.com` 需与后端 HTTPS 一致并在小程序后台配置合法域名 |

---

## 十、审查关注点建议（给 ChatGPT 的检查清单）

1. **去 Redis 的正确性**：内存黑名单并发安全（`ConcurrentHashMap`）、自动续期时 `activeJtiByUser` 是否会误拉黑当前 token（当前实现：续期时旧 jti 等于当前 jti，`recordActiveJti` 内 `oldJti.equals(jti)` 判定避免自拉黑）。
2. **401 重登安全性**：`silentRelogin` 只调白名单接口，不会形成 401 递归；仅重试 1 次防循环；重登成功用 `requestData` 快照重发原请求（GET/POST 均可，幂等性由业务保证）。
3. **code 一次性**：登录接口是否可能并发重放（同一 code 两次调用，第二次微信返回 40163 → `WECHAT_LOGIN_ERROR`，可接受）。
4. **JWT 密钥**：dev 有默认 `jwt.secret` 占位，prod 走 `JWT_SECRET`，确认不会泄露。
5. **自动续期**：`X-New-Token` 是否在小程序端所有响应都被正确消费（目前 request.js 已处理，`_upload` 未消费，但上传响应也带 header——可确认是否需要）。
6. **手机号残留**：确认保留的 mobile 相关代码不构成 getPhoneNumber 解密逻辑。

---

## 附：文件路径索引

**后端**：`pom.xml`、`src/main/resources/application{,-dev,-prod}.properties`、`src/main/java/com/ldz/park/service/{AuthTokenService,UserService}.java`、`model/request/LoginForm.java`、`security/{UserSecurityInterceptor,InterceptorConfig}.java`、`util/JwtUtil.java`、`web/wechat/WechatAuthController.java`、`docs/mini-login.md`、`docs/business-review.md`（本文档）
**小程序**：`server/request.js`、`server/url.js`、`config/net.config.js`、`app.js`、`pages/login/index.{js,wxml,wxss}`、`api/user.js`
