package com.nuit.yujin.smartcateringbackend.controller;

import com.nuit.yujin.smartcateringbackend.common.Result;
import com.nuit.yujin.smartcateringbackend.entity.User;
import com.nuit.yujin.smartcateringbackend.service.UserService;
import com.nuit.yujin.smartcateringbackend.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

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
        return Result.success(userService.login(params.get("code")));
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
}
