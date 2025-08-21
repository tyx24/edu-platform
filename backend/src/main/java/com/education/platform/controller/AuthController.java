package com.education.platform.controller;

import com.education.platform.dto.LoginRequest;
import com.education.platform.dto.RegisterRequest;
import com.education.platform.entity.User;
import com.education.platform.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册与登录功能
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证模块", description = "注册与登录接口")
public class AuthController {

    @Autowired
    private IUserService userService;

    /**
     * 用户注册接口
     * <p>
     * - 学生注册后默认激活（status=1）
     * - 教师注册后需管理员审核（status=0 待审核）
     */
    @Operation(
            summary = "用户注册",
            description = "支持注册为学生或教师，教师注册需管理员审核。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = User.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误")
            }
    )
    @PostMapping("/register")
    public User register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "注册请求体",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            )
            @RequestBody RegisterRequest request
    ) {
        return userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole()
        );
    }

    /**
     * 用户登录接口
     * <p>
     * - 登录成功返回 JWT Token
     * - 前端需将 token 存储在 localStorage/sessionStorage，并在后续请求中放入 Authorization 头
     */
    @Operation(
            summary = "用户登录",
            description = "输入用户名与密码进行登录，成功返回 JWT Token。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "登录成功，返回 Token", content = @Content(schema = @Schema(example = "{\"token\":\"your-jwt-token\"}"))),
                    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
            }
    )
    @PostMapping("/login")
    public Map<String, String> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "登录请求体",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @RequestBody LoginRequest request
    ) {
        String token = userService.login(request.getUsername(), request.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return response;
    }
}
