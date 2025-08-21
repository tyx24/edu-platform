package com.education.platform.controller;

import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.Course;
import com.education.platform.service.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程控制器
 * 处理课程发布、审核、查询等功能
 */
@RestController
@RequestMapping("/course")
@Tag(name = "课程模块", description = "课程发布、审核、查询接口")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    /**
     * 教师发布课程
     * - 教师提交课程后进入待审核状态（state=0）
     */
    @Operation(
            summary = "教师发布课程",
            description = "仅教师角色可调用，发布后课程进入待审核状态。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "发布成功，返回提示信息", content = @Content(schema = @Schema(example = "{\"code\":200,\"msg\":\"课程提交成功，等待审核\"}"))),
                    @ApiResponse(responseCode = "401", description = "未登录"),
                    @ApiResponse(responseCode = "403", description = "权限不足，仅教师可发布")
            }
    )
    @PostMapping("/create")
    @PreAuthorize("hasRole('teacher')")
    public Map<String, Object> createCourse(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "课程信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Course.class))
            )
            @RequestBody Course course
    ) {
        Map<String, Object> result = new HashMap<>();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        course.setTeacherId(userId);
        course.setState((byte) 0); // 待审核
        courseService.save(course);

        result.put("code", 200);
        result.put("msg", "课程提交成功，等待审核");
        return result;
    }

    /**
     * 管理员审核通过课程
     */
    @Operation(
            summary = "管理员审核通过课程",
            description = "仅管理员可调用，审核通过后课程进入已发布状态。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "审核通过", content = @Content(schema = @Schema(example = "审核通过"))),
                    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可审核")
            }
    )
    @PostMapping("/approve/{courseId}")
    @PreAuthorize("hasRole('admin')")
    public String approve(
            @Parameter(description = "课程ID", example = "1001") @PathVariable Long courseId
    ) {
        courseService.approveCourse(courseId);
        return "审核通过";
    }

    /**
     * 管理员驳回课程
     */
    @Operation(
            summary = "管理员驳回课程",
            description = "仅管理员可调用，驳回后课程状态为关闭。",
            responses = {
                    @ApiResponse(responseCode = "200", description = "审核驳回", content = @Content(schema = @Schema(example = "审核驳回"))),
                    @ApiResponse(responseCode = "403", description = "权限不足，仅管理员可审核")
            }
    )
    @PostMapping("/reject/{courseId}")
    @PreAuthorize("hasRole('admin')")
    public String reject(
            @Parameter(description = "课程ID", example = "1001") @PathVariable Long courseId
    ) {
        courseService.rejectCourse(courseId);
        return "审核驳回";
    }

//    /**
//     * 学生查看已发布课程
//     */
//    @Operation(
//            summary = "查看已发布课程",
//            description = "所有用户均可调用，返回已发布的课程列表。",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "课程列表", content = @Content(schema = @Schema(implementation = Course.class)))
//            }
//    )
//    @GetMapping("/published")
//    public List<Course> list() {
//        return courseService.listPublishedCourses();
//    }

    /**
     * 获取课程详情
     * - 返回课程基本信息、平均评分、评论数和评论列表
     */
    @Operation(summary = "获取课程详情", description = "返回课程基本信息、平均评分、评论数和评论列表")
    @ApiResponse(responseCode = "200", description = "课程详情获取成功")
    @GetMapping("/detail/{courseId}")
    public Map<String, Object> getCourseDetail(@PathVariable Long courseId) {
        return courseService.getCourseDetail(courseId);
    }

    /**
     * 分页查询课程
     * - 支持关键词搜索、状态筛选、按教师ID或学生ID筛选
     */
    @Operation(summary = "分页查询课程", description = "支持关键词搜索、状态筛选")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @PostMapping("/list")
    public PageResult<Course> getCourseList(@RequestBody PageRequest request) {
        return courseService.getCourseList(request);
    }


}
