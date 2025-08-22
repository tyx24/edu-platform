package com.education.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.Course;
import com.education.platform.entity.User;
import com.education.platform.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "提供用户信息查询、教师审核、用户管理等接口")
public class UserController {

    @Autowired
    private IUserService userService;

    // ======================= 通用接口 =======================

    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的详细信息")
    @GetMapping("/me")
    public User getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long userId;
        try {
            userId = Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("当前用户信息异常");
        }
        return userService.getById(userId);
    }

    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Serializable id) {
        return userService.getById(id);
    }


    // ======================= 管理员接口 =======================

    @Operation(summary = "审核教师账户", description = "管理员审核教师账号并激活，状态置为启用")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/approve")
    public String approveteacher(
            @Parameter(description = "教师用户ID", required = true)
            @RequestParam(defaultValue = "1") Long userId) {

        User user = userService.getById(userId);
        if (user == null || !"teacher".equals(user.getRole())) {
            throw new RuntimeException("该用户不是教师");
        }
        if (user.getStatus() != 0) {
            throw new RuntimeException("该用户已启用");
        }
        user.setStatus((byte) 1); // 启用
        userService.updateById(user);
        return "审核通过";
    }

    @Operation(summary = "审核教师账户", description = "管理员审核教师账号并拒绝激活，状态置为封禁")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/reject")
    public String rejectTeacher(
            @Parameter(description = "教师用户ID", required = true)
            @RequestParam(defaultValue = "2") Long userId) {

        User user = userService.getById(userId);
        if (user == null || !"teacher".equals(user.getRole())) {
            throw new RuntimeException("该用户不是教师");
        }
        if (user.getStatus() == 1) {
            throw new RuntimeException("该用户已启用");
        }
        user.setStatus((byte) 2); // 启用
        userService.updateById(user);
        return "拒绝通过";
    }

    @Operation(summary = "查询待审核教师列表", description = "管理员查询所有待审核（status=0）的教师账户")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin/pending-teachers")
    public List<User> getPendingteachers() {
        return userService.list(new QueryWrapper<User>()
                .eq("role", "teacher")
                .eq("status", 0));
    }

//    @Operation(summary = "分页查询用户", description = "管理员分页查询用户，可选按角色过滤")
//    @PreAuthorize("hasRole('admin')")
//    @GetMapping("/admin/page")
//    public Page<User> getUsersByPage(
//            @Parameter(description = "当前页码", example = "1")
//            @RequestParam(defaultValue = "1") int page,
//            @Parameter(description = "每页条数", example = "10")
//            @RequestParam(defaultValue = "10") int size,
//            @Parameter(description = "角色筛选，可选 student/teacher/admin", required = false)
//            @RequestParam(required = false) String role,
//            @Parameter(description = "状态筛选，可选 0冻结 / 1正常 / 2封禁", required = false)
//            @RequestParam(required = false) String status
//    ) {
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        if (role != null && !role.isEmpty()) {
//            wrapper.eq("role", role);
//        }
//        if (status != null && !status.isEmpty()) {
//            wrapper.eq("status", status);
//        }
//        return userService.page(new Page<>(page, size), wrapper);
//    }


    @Operation(summary = "分页查询用户", description = "管理员分页查询用户，可选按角色过滤")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/page")
    public PageResult<User> getUsersByPage(@RequestBody PageRequest request,
                                           @Parameter(description = "角色筛选，可选 student/teacher/admin", required = false)
                                           @RequestParam(required = false) String role
    ) {
        return userService.getUserList(request, role);
    }

    @Operation(summary = "修改用户状态",
            description = "管理员根据用户ID修改用户状态，0冻结 / 1正常 / 2封禁")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/status")
    public String changeUserStatus(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "状态：0冻结 / 1正常 / 2封禁",
                    required = true, example = "1")
            @RequestParam byte status) {

        if (status != 0 && status != 1 && status != 2)
            throw new RuntimeException("状态无效");

        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        user.setStatus(status);
        userService.updateById(user);

        switch (status) {
            case 0:
                return "已冻结";
            case 1:
                return "已恢复正常";
            case 2:
                return "已封禁";
            default:
                return "操作完成";
        }
    }

    @Operation(summary = "修改用户角色", description = "管理员修改指定用户角色，可选 student/teacher/admin")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/update-role")
    public String updateRole(
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "角色：student/teacher/admin", required = true)
            @RequestParam String role) {

        if (!role.equals("student") && !role.equals("teacher") && !role.equals("admin")) {
            throw new RuntimeException("角色无效");
        }

        User user = userService.getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        user.setRole(role);
        userService.updateById(user);
        return "角色已更新为: " + role;
    }
}