package com.education.platform.controller;

import com.education.platform.entity.Exam;
import com.education.platform.service.IExamService;
import com.education.platform.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 考试表 前端控制器，提供创建、更新、删除和获取考试列表的功能
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/exam")
@Tag(name = "考试管理", description = "考试管理相关操作")
public class ExamController {

    @Autowired
    private IExamService examService;

    /**
     * 教师创建考试
     *
     * @param exam 考试对象
     * @return 创建成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @PostMapping
    @Operation(
            summary = "创建考试",
            description = "教师通过此接口创建新的考试",
            responses = {
                    @ApiResponse(responseCode = "200", description = "考试创建成功"),
                    @ApiResponse(responseCode = "400", description = "考试信息无效")
            }
    )
    public R<Void> create(@RequestBody @Parameter(description = "考试信息") Exam exam) {
        examService.createExam(exam);
        return R.ok();
    }

    /**
     * 教师更新考试信息
     *
     * @param id   考试ID
     * @param exam 更新后的考试对象
     * @return 更新成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @PutMapping("/{id}")
    @Operation(
            summary = "更新考试",
            description = "教师通过此接口更新已存在的考试信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "考试信息更新成功"),
                    @ApiResponse(responseCode = "404", description = "考试未找到")
            }
    )
    public R<Void> update(
            @PathVariable @Parameter(description = "考试ID") Long id,
            @RequestBody @Parameter(description = "更新后的考试信息") Exam exam
    ) {
        exam.setId(id);
        examService.updateExam(exam);
        return R.ok();
    }

    /**
     * 教师删除考试
     *
     * @param id 考试ID
     * @return 删除成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除考试",
            description = "教师通过此接口删除指定的考试",
            responses = {
                    @ApiResponse(responseCode = "200", description = "考试删除成功"),
                    @ApiResponse(responseCode = "404", description = "考试未找到")
            }
    )
    public R<Void> delete(@PathVariable @Parameter(description = "考试ID") Long id) {
        examService.deleteExam(id);
        return R.ok();
    }

    /**
     * 根据课程ID获取考试列表
     *
     * @param courseId 课程ID
     * @return 该课程下的所有考试列表
     */
    @PreAuthorize("hasAnyRole('teacher','student','admin')")
    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "获取课程考试列表",
            description = "教师和学生通过此接口获取某课程的所有考试信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取考试列表"),
                    @ApiResponse(responseCode = "404", description = "没有找到该课程的考试")
            }
    )
    public R<List<Exam>> listByCourse(@PathVariable @Parameter(description = "课程ID") Long courseId) {
        return R.ok(examService.listByCourse(courseId));
    }
}
