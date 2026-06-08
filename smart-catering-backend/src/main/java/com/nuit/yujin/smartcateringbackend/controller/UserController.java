package com.nuit.yujin.smartcateringbackend.controller;

import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.User;
import com.nuit.yujin.smartcateringbackend.service.UserService;
import com.nuit.yujin.smartcateringbackend.utils.JwtUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        if (params == null) {
            throw new RuntimeException("请求体不能为空");
        }
        String code = firstNotBlank(params.get("code"), params.get("username"), params.get("account"));
        return Result.success(userService.login(code));
    }

    @GetMapping("/mock-login")
    public Result<Map<String, Object>> mockLogin(@RequestParam String code) {
        return Result.success(userService.login(code));
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        return Result.success(userService.getById(jwtUtils.getUserId(token)));
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody User updateData) {
        User user = new User();
        user.setId(jwtUtils.getUserId(token));
        user.setNickname(updateData.getNickname());
        user.setAvatarUrl(updateData.getAvatarUrl());
        user.setPhone(updateData.getPhone());
        userService.updateById(user);
        return Result.success(null);
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        throw new RuntimeException("登录账号不能为空");
    }
}
