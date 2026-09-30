# 微信小程序静默登录接入说明

面向对象：小程序前端开发、后端联调、运维。

本项目提供**微信小程序静默登录**：用户无需手动点击授权，小程序启动时用 `wx.login()` 拿到的临时 code 换取后端 JWT，实现无感登录。

---

## 一、时序

```
小程序                            后端 (park-manger-java)                       微信服务器
  │                                    │                                         │
  │─ wx.login() ─────────────────────► │                                         │
  │◄── code ─────────────────────────  │                                         │
  │                                                                              │
  │─ POST /api/oauth/mini/login  {code, token?} ──►                              │
  │                                    │─ GET sns/jscode2session (appid/secret/code) ─►
  │                                    │◄── { openid, session_key, ... } ─────  │
  │                                    │                                         │
  │                                    │  查/建 User by openid                    │
  │                                    │  签发 mini 通道 JWT                      │
  │                                    │  (若带旧 token，加入 jti 黑名单)         │
  │                                    │                                         │
  │◄─ { code:200, data:{ id, nickname, mobile, avatar, token, ... } } ──         │
  │                                                                              │
  │─ 之后所有请求：Authorization: Bearer <token> ─►                                │
```

---

## 二、后端配置

`UserService` 通过 `@Value` 读取以下两个环境变量：

| 变量 | 说明 |
|---|---|
| `WECHAT_MINIAPP_APP_ID` | 小程序 AppID |
| `WECHAT_MINIAPP_SECRET` | 小程序 AppSecret |

`UserService` 通过 `@Value("${WECHAT_MINIAPP_APP_ID:}")` / `@Value("${WECHAT_MINIAPP_SECRET:}")` 从环境变量或系统属性中读取，**properties 文件中不写占位**（否则会与同名环境变量形成循环引用，导致启动失败：`Circular placeholder reference`）。

**注入方式**：通过部署环境变量（Docker `-e`、K8s Secret、systemd EnvironmentFile、CI/CD 部署脚本注入等）。不要把真实值提交到仓库。

未配置时接口会返回错误：`{ code: "WECHAT_CONFIG_ERROR", message: "微信登录未配置" }`。

---

## 三、接口契约

### 1. 统一登录/续期

- 路径：`POST /api/oauth/mini/login`
- 白名单：已在 `security/InterceptorConfig.java` 中放行，无需鉴权
- 请求体：

```json
{
  "code": "<wx.login 返回的 code>",
  "token": "<可选，旧 token，用于将旧 jti 拉黑>",
  "name": "<可选，首次建号时的昵称，默认为 '微信用户'>"
}
```

- 响应（成功）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "nickname": "微信用户",
    "avatar": null,
    "mobile": null,
    "description": null,
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

字段以 `model/vo/SimpleUser.java` 为准。`SimpleUser` 使用 `@JsonInclude(NON_NULL)`，为空的字段会被剔除。

- 错误码：

| code | 场景 |
|---|---|
| `WECHAT_CONFIG_ERROR` | 后端未注入 AppID / Secret |
| `WECHAT_NETWORK_ERROR` | 微信 jscode2session 接口网络异常 / 无响应 |
| `WECHAT_LOGIN_ERROR`  | jscode2session 返回 errcode 非 0（如 40029 code 无效、40163 code 已使用）/ 响应解析失败 / openid 为空 |

### 2. 兼容旧接口

- 路径：`POST /api/wx-login`
- 行为：内部转调 `miniLogin`，与 `/api/oauth/mini/login` **完全等价**
- 建议：新客户端统一走 `/api/oauth/mini/login`，`/api/wx-login` 仅保留兼容

### 3. 登出

- 路径：`POST /api/oauth/logout`
- 请求头：`Authorization: Bearer <token>`
- 行为：把当前 token 的 jti 加入黑名单

---

## 四、Token 与 Session 策略

> 本项目为**纯 JWT 无状态方案，不依赖 Redis**。黑名单使用进程内内存实现，
> 应用重启后黑名单清空（已登出/被顶的 token 在自身 `exp` 到期前可重新使用，对个人小程序可接受）。

- 通道：`mini`（对应 `JwtUtil.CHANNEL_MINI`）
- 有效期：由 `jwt.mini.ttl` 决定，默认 `2592000` 秒（30 天）
- **单设备互踢**：同一用户再次登录会签发新 token 并把旧 jti 拉黑；老 token 后续请求会被 `UserSecurityInterceptor` 拒绝
- 黑名单：`AuthTokenService` 内存 `ConcurrentHashMap<jti, 过期时间>`，用于主动登出、被顶下线、旧 token 拉黑
- 强制下线：`AuthTokenService.forceLogout(userId, channel, ttl)` 定位该用户当前 jti 并拉黑

---

## 五、小程序端最小示例

### `app.js`

```js
App({
  onLaunch() {
    this.silentLogin();
  },

  silentLogin() {
    const oldToken = wx.getStorageSync('token') || '';

    wx.login({
      success: (res) => {
        if (!res.code) {
          console.error('wx.login failed', res);
          return;
        }
        wx.request({
          url: 'https://<你的后端域名>/api/oauth/mini/login',
          method: 'POST',
          data: { code: res.code, token: oldToken },
          success: (resp) => {
            const body = resp.data || {};
            if (body.code === 200 && body.data && body.data.token) {
              wx.setStorageSync('token', body.data.token);
              wx.setStorageSync('user', body.data);
            } else {
              console.error('mini login failed', body);
            }
          },
          fail: (err) => console.error('mini login network error', err),
        });
      },
    });
  },
});
```

### 后续业务请求携带 token

```js
function apiRequest(options) {
  const token = wx.getStorageSync('token');
  return new Promise((resolve, reject) => {
    wx.request({
      ...options,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...(options.header || {}),
      },
      success: (resp) => {
        // 收到 401 或后端返回登录失效码 -> 重新走 wx.login 静默登录一次再重试
        if (resp.statusCode === 401) {
          getApp().silentLogin();
        }
        resolve(resp);
      },
      fail: reject,
    });
  });
}
```

### 过期/失效处理建议

- 请求返回 401 或后端约定的登录失效 code 时：
  1. 清掉本地 token
  2. 再次调用 `wx.login()` 拿新 code
  3. 携带旧 token（若还在本地）请求 `/api/oauth/mini/login`，后端会拉黑旧 jti、下发新 token
  4. 重试原请求

---

## 六、字段兼容说明

`LoginForm` 中保留了以下字段但**服务端不读取**，仅为向前兼容：

- `wxUserId`：已标 `@Deprecated`，服务端不再读取
- `account` / `password`：账号密码登录使用，静默登录忽略

小程序端建议只发送 `code`（首次建号可选带 `name`）。

---

## 七、验证方式

配置好 AppID / Secret，本地跑起服务后：

```bash
curl -X POST http://localhost:3429/api/oauth/mini/login \
  -H 'Content-Type: application/json' \
  -d '{"code":"<小程序调 wx.login 拿到的真实 code>"}'
```

预期：返回 `{ code: 200, data: { token, ... } }`；带旧 token 登录时旧 jti 会进入内存黑名单。

无真实 code 时可先验证配置：如果响应是 `WECHAT_LOGIN_ERROR` 而非 `WECHAT_CONFIG_ERROR`，说明 AppID/Secret 已生效。
