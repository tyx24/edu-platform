package com.education.platform.controller;

import com.education.platform.entity.ExamRecord;
import com.education.platform.service.IExamRecordService;
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

import java.util.List;

/**
 * <p>
 * 考试记录表 前端控制器，提供学生提交考试记录、教师查看考试记录等功能
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@RestController
@RequestMapping("/examRecord")
@Tag(name = "考试记录", description = "考试记录管理相关操作")
public class ExamRecordController {

    @Autowired
    private IExamRecordService examRecordService;

    /**
     * 学生提交考试记录
     *
     * @param record 考试记录
     * @return 提交成功的响应
     */
    @PreAuthorize("hasRole('student')")
    @PostMapping("/submit")
    @Operation(
            summary = "提交考试记录",
            description = "学生通过此接口提交自己的考试记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "考试记录提交成功"),
                    @ApiResponse(responseCode = "400", description = "提交失败")
            }
    )
    public R<Void> submitExam(@RequestBody @Parameter(description = "考试记录信息") ExamRecord record) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        record.setStudentId(userId);
        examRecordService.submitExam(record);
        return R.ok();
    }

    /**
     * 教师获取某个考试的所有考试记录
     *
     * @param examId 考试ID
     * @return 返回指定考试的所有考试记录
     */
    @PreAuthorize("hasRole('teacher')")
    @GetMapping("/exam/{examId}")
    @Operation(
            summary = "获取考试记录",
            description = "教师通过此接口获取某个考试的所有考试记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取考试记录"),
                    @ApiResponse(responseCode = "404", description = "没有找到考试记录")
            }
    )
    public R<List<ExamRecord>> listByExam(@PathVariable @Parameter(description = "考试ID") Long examId) {
        List<ExamRecord> list = examRecordService.lambdaQuery()
                .eq(ExamRecord::getExamId, examId)
                .list();
        return R.ok(list);
    }

    /**
     * 获取学生自己的考试记录
     *
     * @param examId 考试ID
     * @return 返回学生自己的考试记录
     */
    @PreAuthorize("hasRole('student')")
    @GetMapping("/my/{examId}")
    @Operation(
            summary = "获取学生的考试记录",
            description = "学生通过此接口获取自己在某个考试中的考试记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功获取学生考试记录"),
                    @ApiResponse(responseCode = "404", description = "没有找到该考试记录")
            }
    )
    public R<ExamRecord> getMyRecord(@PathVariable @Parameter(description = "考试ID") Long examId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());
        ExamRecord record = examRecordService.lambdaQuery()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getStudentId, userId)
                .one();
        return R.ok(record);
    }
}