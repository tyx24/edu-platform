package com.education.platform.controller;

import com.education.platform.entity.Submission;
import com.education.platform.service.ISubmissionService;
import com.education.platform.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 作业提交表 前端控制器，提供学生作业上传、提交，教师评分和查询功能
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/submission")
@Tag(name = "作业提交记录", description = "作业提交与评分相关操作")
public class SubmissionController {
    @Autowired
    private ISubmissionService submissionService;

    /**
     * 上传作业文件（本地存储模拟 OSS）
     * 学生通过此接口上传作业文件
     *
     * @param file 上传的作业文件
     * @return 上传文件后的 URL 地址
     */
    @PreAuthorize("hasRole('student')")
    @PostMapping("/upload")
    @Operation(
            summary = "上传作业文件",
            description = "学生上传作业文件，文件会保存到本地服务器，并返回文件的 URL 地址",
            responses = {
                    @ApiResponse(responseCode = "200", description = "文件上传成功"),
                    @ApiResponse(responseCode = "400", description = "文件为空"),
                    @ApiResponse(responseCode = "500", description = "文件上传失败")
            }
    )
    public R<String> uploadHomeworkFile(@RequestParam("file") @Parameter(description = "上传的作业文件") MultipartFile file) {
        if (file.isEmpty()) {
            return R.fail("上传文件不能为空");
        }
        try {
            // 存储目录
            String uploadDir = System.getProperty("user.dir") + "/upload/homework/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID() + suffix;

            // 保存文件
            File dest = new File(uploadDir + newFileName);
            file.transferTo(dest);

            // 返回文件的访问 URL（这里是本地访问路径）
            String fileUrl = "/files/homework/" + newFileName;
            return R.ok(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return R.fail("文件上传失败");
        }
    }

    /**
     * 学生提交作业
     *
     * @param submission 提交的作业内容
     * @return 提交成功的响应
     */
    @PreAuthorize("hasRole('student')")
    @PostMapping
    @Operation(
            summary = "提交作业",
            description = "学生提交作业内容，提交成功后作业进入批改流程",
            responses = {
                    @ApiResponse(responseCode = "200", description = "作业提交成功"),
                    @ApiResponse(responseCode = "400", description = "提交失败")
            }
    )
    public R<Void> submit(@RequestBody @Parameter(description = "作业提交内容") Submission submission) {
        submissionService.submitHomework(submission);
        return R.ok();
    }

    /**
     * 教师给作业评分
     *
     * @param id      作业提交记录的 ID
     * @param score   评分
     * @param comment 评分评论（可选）
     * @return 评分成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @PostMapping("/{id}/score")
    @Operation(
            summary = "给作业评分",
            description = "教师通过此接口为学生的作业评分，并可以附加评论",
            responses = {
                    @ApiResponse(responseCode = "200", description = "评分成功"),
                    @ApiResponse(responseCode = "400", description = "评分失败"),
                    @ApiResponse(responseCode = "404", description = "作业提交未找到")
            }
    )
    public R<Void> grade(
            @PathVariable @Parameter(description = "作业提交记录的 ID") Long id,
            @RequestParam @Parameter(description = "评分") Integer score,
            @RequestParam(required = false) @Parameter(description = "评分评论") String comment
    ) {
        submissionService.gradeSubmission(id, score, comment);
        return R.ok();
    }

    /**
     * 获取作业提交记录列表（按作业 ID 查询）
     *
     * @param homeworkId 作业 ID
     * @return 返回作业提交记录列表
     */
    @PreAuthorize("hasRole('teacher')")
    @GetMapping("/homework/{homeworkId}")
    @Operation(
            summary = "获取作业提交记录",
            description = "教师通过此接口查询某作业的所有提交记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取作业提交记录"),
                    @ApiResponse(responseCode = "404", description = "没有找到作业提交记录")
            }
    )
    public R<List<Submission>> listByHomework(@PathVariable @Parameter(description = "作业 ID") Long homeworkId) {
        return R.ok(submissionService.listByHomework(homeworkId));
    }

    /**
     * 获取学生的作业提交记录
     *
     * @return 返回当前学生的作业提交记录列表
     */
    @PreAuthorize("hasRole('stuident')")
    @GetMapping("/my")
    @Operation(
            summary = "获取学生作业提交记录",
            description = "学生通过此接口查询自己的作业提交记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取学生作业提交记录"),
                    @ApiResponse(responseCode = "404", description = "没有找到学生的作业提交记录")
            }
    )
    public R<List<Submission>> mySubmissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());
        return R.ok(submissionService.listMySubmissions(studentId));
    }
}
