package com.ldz.park.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ldz.park.dao.UserMapper;
import com.ldz.park.model.SearchForm;
import com.ldz.park.model.User;
import com.ldz.park.model.domain.LogLogin;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.model.meta.ServerException;
import com.ldz.park.model.request.LoginForm;
import com.ldz.park.model.vo.SimpleUser;
import com.ldz.park.util.JwtUtil;
import com.ldz.park.util.TokenHelper;
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
    private ObjectMapper objectMapper;

    @Value("${WECHAT_MINIAPP_APP_ID:}")
    private String wechatAppId;

    @Value("${WECHAT_MINIAPP_SECRET:}")
    private String wechatSecret;

    public List<User> list() {
        return userMapper.getListAll();
    }

    // public ApiResponse getUserList(SearchForm form){
    // ApiResponse apiResponse= new ApiResponse();
    // List<User> list= userMapper.getUserList(form);
    // apiResponse.setData(list);
    // return apiResponse;
    // }

    public SimpleUser login(String account) {
        User user = userMapper.getUserByAccount(account);
        LogLogin log = new LogLogin();
        log.setUser_id(user.getId());
        log.setNickname(user.getName());
        log.setChannel(1);
        log.setType(1);
        String token = jwtUtil.generateToken(user.getId());
        return new SimpleUser().fromUser(user, token);
    }

    public SimpleUser wxLogin(LoginForm loginForm) {
        String account = resolveWechatAccount(loginForm.getCode());
        String mobile = loginForm.getMobile();
        String name = loginForm.getName();
        User user = userMapper.getUserByAccount(account);
        if (user == null && mobile != null && !mobile.isBlank()) {
            user = userMapper.getUserByMobile(mobile);
        }
        if (user == null) {
            user = new User();
            user.setAccount(account);
            user.setName(name == null || name.isBlank() ? "微信用户" : name);
            user.setDoorplate("");
            user.setPassword("123456");
            user.setIsAdmin(0);
            user.setMobile(mobile);
            userMapper.add(user);
            user = userMapper.getUserByAccount(account);
        }
        String token = jwtUtil.generateToken(user.getId());
        return new SimpleUser().fromUser(user, token);
    }

    private String resolveWechatAccount(String code) {
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
            return "wx_" + openid;
        } catch (ServerException e) {
            throw e;
        } catch (Exception e) {
            throw new ServerException("WECHAT_LOGIN_ERROR", "微信登录失败");
        }
    }

    // 已废弃，使用 JWT token 系统，不再需要此方法
    // private SimpleUser getUserLoginResponse(User user) {
    // String token = TokenHelper.getGUID();
    // userMapper.insertToken(user.getId(), token);
    // return new SimpleUser().fromUser(user, token);
    // }

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

    // public void deleteFailUser(String user_name) {
    // userMapper.deleteFailUser(user_name);
    // }

    // 已废弃，现在使用 JWT token 系统，不再通过数据库验证 token
    // 如需使用旧的 token 系统，请调用此方法
    @Deprecated
    public Integer getUserIdByToken(String token) {
        return userMapper.getUserIdByToken(token);
    }
}
