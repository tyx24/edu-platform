package com.education.platform.controller;

import com.education.platform.dto.CourseCommentRequest;
import com.education.platform.entity.CourseComment;
import com.education.platform.service.ICourseCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "课程评论管理", description = "课程评论/评价相关接口")
@RestController
@RequestMapping("/comments")
public class CourseCommentController {

    @Autowired
    private ICourseCommentService commentService;

    @Operation(summary = "提交课程评论", description = "仅学生可调用，提交评分与评论")
    @ApiResponse(responseCode = "200", description = "评论提交成功")
    @PreAuthorize("hasRole('student')") // 只有学生角色能调用
    @PostMapping
    public String addComment(@RequestBody CourseCommentRequest request) {
        // 从 Spring Security 获取当前用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName());
        commentService.addComment(studentId, request);
        return "评论提交成功";
    }

    @Operation(summary = "查询课程评论", description = "任何人都可以查看某课程的所有评论")
    @ApiResponse(responseCode = "200", description = "返回课程评论列表")
    @GetMapping("/{courseId}")
    public List<CourseComment> getComments(@PathVariable Long courseId) {
        return commentService.getCommentsByCourseId(courseId);
    }

    @Operation(summary = "获取课程评论统计信息", description = "返回平均评分与评论总数")
    @ApiResponse(responseCode = "200", description = "返回平均评分和评论数")
    @GetMapping("/stats/{courseId}")
    public Map<String, Object> getCourseCommentStats(@PathVariable Long courseId) {
        return commentService.getCourseCommentStats(courseId);
    }
}
