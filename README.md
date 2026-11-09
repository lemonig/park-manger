# 车位租售管理系统（后台管理）

## 项目概述
本项目是一个地下车位租售管理系统，主要用于管理员管理地下车位信息，用户发布租售车位信息。管理员可以查看、编辑和删除车位信息，审核用户发布的租售信息。

## 技术栈
- **后端**：Spring Boot
- **数据库**：MySQL
- **安全**：JWT认证
- **依赖管理**：Maven
- **文档生成**：Swagger

## 功能模块
1. **用户管理**：
   - 用户注册与登录。
   - 管理员可查看、修改用户信息。
2. **车位管理**：
   - 录入车位信息：车位编号、类型、状态、业主信息等。
   - 车位状态更新：如空闲、已租、已售等。
   - 用户发布的车位信息管理（审核、删除等）。
3. **数据统计与报表**：
   - 车位租赁情况统计。
   - 收入报表。

## 系统部署

### 1. 环境要求
- **Java 21+**
- **MySQL 5.7+**
- **Maven 3.6+**

### 2. 配置环境变量
所有密钥与真实连接串一律通过环境变量注入，仓库内不保存任何明文密钥（历史版本曾泄露并公开，已全部移除）：

| 环境变量 | 用途 | 说明 |
| --- | --- | --- |
| `MYSQL_URL` | JDBC 连接串 | prod 必填；dev 缺省时连本机 `127.0.0.1:3306/park` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | 数据库账号密码 | prod 必填 |
| `JWT_SECRET` | JWT 签名密钥 | prod 必填，不少于 32 字节；更换后所有已发 token 失效 |
| `COS_SECRET_ID` / `COS_SECRET_KEY` | 腾讯云 COS 密钥 | 使用文件上传时必填 |
| `COS_BUCKET` / `COS_REGION` / `COS_BASE_URL` | COS 桶配置 | 有默认值，可覆盖 |
| `WECHAT_MINIAPP_APP_ID` / `WECHAT_MINIAPP_SECRET` | 微信小程序登录 | 使用小程序登录时必填 |

prod profile 对必填项不提供任何默认值，缺失时应用会在启动阶段直接失败；dev profile 缺省指向本机。本地开发示例（勿写入仓库）：

```bash
export MYSQL_PASSWORD='your-local-password'
export JWT_SECRET="$(openssl rand -base64 48)"
bash ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

