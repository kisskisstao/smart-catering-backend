package com.nuit.yujin.smartcateringbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuit.yujin.smartcateringbackend.entity.User;
import com.nuit.yujin.smartcateringbackend.mapper.UserMapper;
import com.nuit.yujin.smartcateringbackend.utils.JwtUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    private final JwtUtils jwtUtils;

    public UserService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> login(String code) {
        if (code == null || code.isBlank()) {
            throw new RuntimeException("code不能为空");
        }

        String openid = "mock_openid_" + code;
        User user = this.lambdaQuery().eq(User::getOpenid, openid).one();

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户_" + code);
            user.setStatus("NORMAL");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            this.save(user);
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        this.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtils.generateToken(user.getId(), user.getNickname()));
        result.put("user", user);
        return result;
    }
}
