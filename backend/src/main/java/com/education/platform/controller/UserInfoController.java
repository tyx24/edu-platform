package com.education.platform.controller;

import com.education.platform.dto.BasicInfoDTO;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.UserInfo;
import com.education.platform.service.IUserInfoService;
import com.education.platform.util.R;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
@Tag(name = "用户信息表", description = "提供用户信息查询和编辑")
public class UserInfoController {

    @Autowired
    private IUserInfoService userInfoService;


    @Operation(summary = "分页查询用户详细信息", description = "可以分页查询用户详细信息")
    @PostMapping("/page")
    public PageResult<UserInfo> getUsersByPage(@RequestBody PageRequest request) {
        return userInfoService.getUserList(request);
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像", description = "用户上传头像文件，保存到本地 OSS，并更新 user_info 表，返回头像访问地址")
    public String uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("头像文件不能为空");
        }

        // 1. 按日期生成目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd"));
        String uploadDir = System.getProperty("user.dir") + "/upload/avatar/" + datePath;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 2. 原始文件名 & 后缀
        String originalName = file.getOriginalFilename();
        String suffix = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String fileName = System.currentTimeMillis() + suffix;

        // 3. 保存文件
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        // 4. 构建可访问 URL（需配合 WebMvcConfigurer 静态映射）
        String fileUrl = "/files/avatar/" + datePath + "/" + fileName;

        // 5. 更新数据库 user_info 表
        UserInfo info = userInfoService.getByUserId(userId);
        if (info == null) {
            info = new UserInfo();
            info.setUserId(userId);
            info.setAvatar(fileUrl);
            userInfoService.saveOrUpdate(info);
        } else {
            info.setAvatar(fileUrl);
            userInfoService.saveOrUpdate(info);
        }

        return fileUrl;
    }

    // ========== 基本信息 ==========
    @PostMapping("/updateBasicInfo")
    @Operation(summary = "更新基本信息")
    public R<Object> updateBasicInfo(@RequestBody BasicInfoDTO request) {
        return userInfoService.updateBasicInfo(request);
    }

    // ========== 联系方式 ==========
    @PostMapping("/updateContactInfo")
    @Operation(summary = "更新联系方式")
    public R<Object> updateContactInfo(@RequestBody UserInfo request) {
        return userInfoService.updateContactInfo(request);
    }

    // ========== 学生信息 ==========
    @PostMapping("/updateStudentInfo")
    @Operation(summary = "更新学生信息")
    @PreAuthorize("hasRole('student')")
    public R<Object> updateStudentInfo(@RequestBody UserInfo request) {
        return userInfoService.updateStudentInfo(request);
    }

    // ========== 教师信息 ==========
    @PostMapping("/updateTeacherInfo")
    @Operation(summary = "更新教师信息")
    @PreAuthorize("hasRole('teacher')")
    public R<Object> updateTeacherInfo(@RequestBody UserInfo request) {
        return userInfoService.updateTeacherInfo(request);
    }

    // ========== 个人简介 ==========
    @PostMapping("/updateBio")
    @Operation(summary = "更新个人简介")
    public R<Object> updateBio(@RequestBody UserInfo request) {
        return userInfoService.updateBio(request);
    }

}
