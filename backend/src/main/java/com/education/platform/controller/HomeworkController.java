package com.education.platform.controller;

import com.education.platform.entity.Homework;
import com.education.platform.service.IHomeworkService;
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
 * 作业表 前端控制器，提供作业创建、更新、删除以及查询功能
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/homework")
@Tag(name = "作业管理", description = "作业管理相关操作")
public class HomeworkController {

    @Autowired
    private IHomeworkService homeworkService;

    /**
     * 教师创建作业
     *
     * @param homework 作业对象
     * @return 创建成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @PostMapping
    @Operation(
            summary = "创建作业",
            description = "教师通过此接口创建新的作业",
            responses = {
                    @ApiResponse(responseCode = "200", description = "作业创建成功"),
                    @ApiResponse(responseCode = "400", description = "作业信息无效")
            }
    )
    public R<Void> create(@RequestBody @Parameter(description = "作业信息") Homework homework) {
        homeworkService.createHomework(homework);
        return R.ok();
    }

    /**
     * 教师更新作业信息
     *
     * @param id       作业ID
     * @param homework 更新后的作业对象
     * @return 更新成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @PutMapping("/{id}")
    @Operation(
            summary = "更新作业",
            description = "教师通过此接口更新已存在的作业信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "作业信息更新成功"),
                    @ApiResponse(responseCode = "404", description = "作业未找到")
            }
    )
    public R<Void> update(
            @PathVariable @Parameter(description = "作业ID") Long id,
            @RequestBody @Parameter(description = "更新后的作业信息") Homework homework
    ) {
        homework.setId(id);
        homeworkService.updateHomework(homework);
        return R.ok();
    }

    /**
     * 教师删除作业
     *
     * @param id 作业ID
     * @return 删除成功的响应
     */
    @PreAuthorize("hasRole('teacher')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除作业",
            description = "教师通过此接口删除指定的作业",
            responses = {
                    @ApiResponse(responseCode = "200", description = "作业删除成功"),
                    @ApiResponse(responseCode = "404", description = "作业未找到")
            }
    )
    public R<Void> delete(@PathVariable @Parameter(description = "作业ID") Long id) {
        homeworkService.deleteHomework(id);
        return R.ok();
    }

    /**
     * 获取某个课程下的所有作业
     *
     * @param courseId 课程ID
     * @return 返回该课程下的所有作业
     */
    @PreAuthorize("hasAnyRole('teacher','student')")
    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "获取课程作业列表",
            description = "教师和学生通过此接口获取某课程的所有作业",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取作业列表"),
                    @ApiResponse(responseCode = "404", description = "没有找到该课程的作业")
            }
    )
    public R<List<Homework>> listByCourse(@PathVariable @Parameter(description = "课程ID") Long courseId) {
        return R.ok(homeworkService.listByCourse(courseId));
    }
}
