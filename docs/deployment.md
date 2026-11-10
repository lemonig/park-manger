# 后端部署流程与运维手册

> 2026-11-10 修订。上一版认知错误(CI 不会自动部署)导致过一次生产事故,本文以当前实际行为为准。

## 一、CI/CD 全链路

```
git push → main (lemonig/park-manger)
   └─ GitHub Actions: .github/workflows/docker-publish.yml.yml
        ├─ Build  两阶段构建(Temurin 21, -DskipTests)
        ├─ Push   ACR: crpi-66pjm0oi7g763epj.cn-hangzhou.personal.cr.aliyuncs.com/park_java/park-java
        │         双 tag: {commit-sha}(用于回滚) + latest(用于部署)
        └─ Deploy SSH(appleboy/ssh-action → secrets.SERVER_HOST)在服务器上执行:
              docker login ACR
              [ -f /data/park/java.env ] || exit 1      ← 守卫:缺配置必须在删旧容器前失败
              docker pull ...:latest
              docker rm -f park-java                     ← 从这里起服务中断
              docker run -d --name park-java --restart unless-stopped \
                -p 3429:3429 -v /data/park/java-logs:/app/logs \
                --env-file /data/park/java.env ...:latest
              docker image prune -f
```

**关键认知:`push 到 main = 立即部署生产`。** 任何改动(包括纯文档、纯 workflow)都会重建镜像并重启容器,后端中断约 15~30 秒。没有测试门禁、没有健康检查门禁。

前端(park-admin)是另一条独立链路:Actions 只推镜像,服务器 cron 每 2 分钟跑 `deploy/auto-deploy.sh` 检测新镜像后重建 `park-nextjs` 容器,不会动后端。

## 二、服务器侧依赖(京东云 36.151.148.156)

| 项 | 说明 |
| --- | --- |
| `/data/park/java.env` | **全部敏感配置,root:600**。缺此文件部署守卫直接失败(旧容器保留,服务不受影响) |
| `/data/park/java-logs/` | 容器日志挂载点(logback 在容器内写 `/app/logs`,属主 uid 100) |
| `/etc/nginx/sites-enabled/kolder.site` | `/api/`→127.0.0.1:3429(后端);`/`→127.0.0.1:3000(前端);`/note/`→127.0.0.1:8080(invest-notebook,另一个项目) |
| 服务器上手动跑的容器 | `park-java`(后端)、`park-nextjs`(前端)、`invest-notebook`(无关项目,勿动) |

`java.env` 必需的 8 个变量:

```
MYSQL_URL            JDBC 连接串(京东云 RDS)
MYSQL_USER / MYSQL_PASSWORD
JWT_SECRET           HS256 签名密钥(≥32 字节;换值 = 全部用户重新登录)
COS_SECRET_ID / COS_SECRET_KEY   腾讯云 COS
WECHAT_MINIAPP_APP_ID / WECHAT_MINIAPP_SECRET
```

⚠️ **轮换任何密钥后必须同步改 `java.env`**,然后 `docker restart park-java`(或重跑一次部署),否则容器重启后连不上数据库。

## 三、流量拓扑(哪些域名真的在用)

- **小程序**:代码里 `server/url.js` 三个环境(develop/trial/release)全部指向 `https://kolder.site`,经本机 nginx → 3429。改请求域名只认 `server/url.js`,`config/net.config.js` 的 baseURL 不参与请求。
- **管理后台**:同样走 `https://kolder.site`。
- `wx-mini.greandata1.com`(解析到 116.62.208.83 阿里云,另一台机器)与本系统运行时**无关**,小程序不请求它。

## 四、Profile 说明(易踩坑)

- 镜像内 `application.properties` 写死 `spring.profiles.active=dev`,docker run 不覆盖 → **容器实际运行 dev profile**。
- 关键值全部由 `java.env` 注入,所以实际行为等同生产;但 dev 下数据库 `useSSL=false`、Swagger 开启。
- 切 prod profile:在 `java.env` 加 `SPRING_PROFILES_ACTIVE=prod` 后重启容器。注意 prod 的 `mysql.url`/`jwt.secret`/COS 密钥**无默认值**,缺任何一个启动即失败(fail-fast 是有意设计)。

## 五、部署后验证清单(每次部署必做)

```bash
# 1. 容器状态:必须是 Up,不能是 Restarting
docker ps --filter name=park-java --format '{{.Names}} {{.Status}}'

# 2. 启动日志:必须见到 Started ParkApplication
docker logs --tail 5 park-java

# 3. 本机探活:期望业务 JSON(用户名或密码错误),404/500 都是不对
curl -s -X POST http://127.0.0.1:3429/api/admin/auth/login \
  -H 'Content-Type: application/json' -d '{"account":"x","password":"x"}'

# 4. 公网全链路(小程序真实域名)
curl -s -X POST https://kolder.site/api/wechat/auth/mini-login \
  -H 'Content-Type: application/json' -d '{"code":"probe-test"}'
# 期望 {"code":400,...,"error":"WECHAT_LOGIN_ERROR"} —— 假 code 被微信拒绝属正常,证明路由+微信凭证全通
```

## 六、回滚

ACR 保留了每个 commit sha 的镜像 tag,回滚 = 用旧 sha 重建容器(env 文件不变):

```bash
IMG=crpi-66pjm0oi7g763epj.cn-hangzhou.personal.cr.aliyuncs.com/park_java/park-java
docker pull $IMG:<出问题前的 sha>
docker rm -f park-java
docker run -d --name park-java --restart unless-stopped -p 3429:3429 \
  -v /data/park/java-logs:/app/logs --env-file /data/park/java.env $IMG:<sha>
# 然后按第五节验证
```

## 七、事故复盘(2026-11-09/10 小程序断联)

1. 安全加固移除了镜像内的默认密钥(配置改 `${ENV:}` 注入)。
2. CI 自动部署新容器时**只注入了微信变量**,数据库默认连容器内 `127.0.0.1` → 连接失败 → Spring 启动即退出 → `restart unless-stopped` 无限重启循环 → 3429 无监听 → 小程序后台全挂。
3. 事故期间误判"CI 只构建镜像不会自动部署"——只读了 workflow 前 20 行,Deploy 步骤在后面。

**教训:**
- 改任何与配置/部署相关的内容前,**读完整个 workflow 再下结论**。
- 移除默认值 = 改变部署契约,必须同步改所有部署入口(本仓库 workflow、服务器手动部署命令),并留 fail-safe(本次的 env 文件守卫)。
- 部署后必须按第五节验证,不能只看 CI 绿灯(CI 绿只代表 SSH 命令执行成功,不代表应用活着)。

## 八、已知限制 / 待改进

- 任何 push 都重启生产容器(15~30s 中断),纯文档改动也一样。可改进:workflow 加 `paths:` 过滤,仅 `src/`、`pom.xml`、`Dockerfile` 变更时才构建部署。
- 部署无健康检查门禁。可改进:ssh script 里部署后 `sleep 20` + curl 探活,失败自动用旧 sha 回滚。
- GitHub Secrets 里的 `WECHAT_MINIAPP_APP_ID/SECRET` 已不再被 workflow 使用(实际生效的是 java.env),可择机清理。
