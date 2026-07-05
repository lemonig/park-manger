package com.ldz.park.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.dao.UserMapper;
import com.ldz.park.model.User;
import com.ldz.park.model.domain.LogLogin;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.meta.ServerException;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${WECHAT_MINIAPP_APP_ID:}")
    private String wechatAppId;

    @Value("${WECHAT_MINIAPP_SECRET:}")
    private String wechatSecret;

    public List<User> list() {
        return userMapper.getListAll();
    }

    public SimpleUser login(String account) {
        User user = userMapper.getUserByAccount(account);
        LogLogin log = new LogLogin();
        log.setUser_id(user.getId());
        log.setNickname(user.getName());
        log.setChannel(1);
        log.setType(1);
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole(user.getIsAdmin() != null && user.getIsAdmin() == 1 ? "admin" : "user");
        }
        String token = authTokenService.issueAdminToken(user);
        return new SimpleUser().fromUser(user, token);
    }

    /**
     * 小程序登录/续期：通过 code 换 openid，查/建用户，返回带 token 的用户信息。
     * 若请求带旧 token，则先将旧 jti 拉黑，避免并存。
     */
    public SimpleUser miniLogin(LoginForm loginForm) {
        String openid = resolveWechatOpenid(loginForm.getCode());
        String mobile = loginForm.getMobile();
        String name = loginForm.getName();

        User user = userMapper.getUserByOpenid(openid);
        if (user == null) {
            User legacy = userMapper.getUserByAccount("wx_" + openid);
            if (legacy != null) {
                legacy.setOpenid(openid);
                legacy.setChannel(JwtUtil.CHANNEL_MINI);
                if (legacy.getRole() == null || legacy.getRole().isBlank()) {
                    legacy.setRole("user");
                }
                userMapper.update(legacy);
                user = legacy;
            }
        }
        if (user == null && mobile != null && !mobile.isBlank()) {
            User byMobile = userMapper.getUserByMobile(mobile);
            if (byMobile != null) {
                byMobile.setOpenid(openid);
                byMobile.setChannel(JwtUtil.CHANNEL_MINI);
                if (byMobile.getRole() == null || byMobile.getRole().isBlank()) {
                    byMobile.setRole("user");
                }
                userMapper.update(byMobile);
                user = byMobile;
            }
        }
        if (user == null) {
            user = new User();
            user.setAccount("wx_" + openid);
            user.setOpenid(openid);
            user.setName(name == null || name.isBlank() ? "微信用户" : name);
            user.setDoorplate("");
            user.setPassword("123456");
            user.setIsAdmin(0);
            user.setChannel(JwtUtil.CHANNEL_MINI);
            user.setRole("user");
            user.setMobile(mobile);
            userMapper.add(user);
            if (user.getId() == null) {
                user = userMapper.getUserByOpenid(openid);
            }
        }

        authTokenService.revokeIfPresent(loginForm.getToken());

        String token = authTokenService.issueMiniToken(user);
        return new SimpleUser().fromUser(user, token);
    }

    /**
     * 老接口 wxLogin 保留，内部转调 miniLogin。
     */
    public SimpleUser wxLogin(LoginForm loginForm) {
        return miniLogin(loginForm);
    }

    private String resolveWechatOpenid(String code) {
        if (wechatAppId == null || wechatAppId.isBlank() || wechatSecret == null || wechatSecret.isBlank()) {
            throw new ServerException("WECHAT_CONFIG_ERROR", "微信登录未配置");
        }
        try {
            String url = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/sns/jscode2session")
                    .queryParam("appid", wechatAppId)
                    .queryParam("secret", wechatSecret)
                    .queryParam("js_code", code)
                    .queryParam("grant_type", "authorization_code")
                    .toUriString();
            String response = new RestTemplate().getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);
            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                throw new ServerException("WECHAT_LOGIN_ERROR", json.path("errmsg").asText("微信登录失败"));
            }
            String openid = json.path("openid").asText("");
            if (openid.isBlank()) {
                throw new ServerException("WECHAT_LOGIN_ERROR", "微信登录失败");
            }
            return openid;
        } catch (ServerException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("WECHAT_LOGIN_ERROR", "微信登录失败");
        }
    }

    public ApiResponse getListAll() {
        List<User> list = userMapper.getListAll();
        return ApiResponse.success(list);
    }

    public User detail(Integer id) {
        return userMapper.detail(id);
    }

    public void add(User user) {
        userMapper.add(user);
    }

    public void update(User user) {
        userMapper.update(user);
    }

    public void delete(Integer id) {
        userMapper.delete(id);
    }

    public void resetPassword(Integer id) {
        userMapper.resetPassword(id, "123456");
    }

    @Deprecated
    public Integer getUserIdByToken(String token) {
        return userMapper.getUserIdByToken(token);
    }
}
