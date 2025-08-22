package com.education.platform.controller;

import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.UserInfo;
import com.education.platform.service.IUserInfoService;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author tan
 * @since 2025-08-22
 */
@RestController
@RequestMapping("/userInfo")
@Tag(name = "用户信息表",description = "提供用户信息查询")
public class UserInfoController {

    @Autowired
    private IUserInfoService iUserInfoService;


    @Operation(summary = "分页查询用户详细信息", description = "可以分页查询用户详细信息")
    @PostMapping("/page")
    public PageResult<UserInfo> getUsersByPage(@RequestBody PageRequest request) {
        return iUserInfoService.getUserList(request);
    }
}
