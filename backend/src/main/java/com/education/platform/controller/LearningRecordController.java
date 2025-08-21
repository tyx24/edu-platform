package com.education.platform.controller;

import com.education.platform.service.ILearningRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 * 学习进度记录表 前端控制器
 * </p>
 *
 * @author tan
 * @since 2025-08-10
 */
@RestController
@RequestMapping("/learningRecord")
@Tag(name = "学习进度管理", description = "学生课程学习进度跟踪相关接口")
public class LearningRecordController {

    @Autowired
    private ILearningRecordService learningRecordService;

    /**
     * 学生上报学习进度
     */
    @Operation(summary = "上报学习进度", description = "学生在学习章节后上报进度")
    @ApiResponse(responseCode = "200", description = "进度更新成功")
    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('student','admin')")
    public String report(@Parameter(description = "资源ID", example = "1") @RequestParam Long resourceId,
                         @Parameter(description = "当前学习位置（单位：秒）", example = "300") @RequestParam Integer currentTime,
                         @Parameter(description = "资源总时长（单位：秒）", example = "1800") @RequestParam Integer duration) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long studentId = Long.parseLong(auth.getName()); // 从 JWT 获取学生 ID

        // 判断是否完成（到达总时长的 95% 以上视为完成）
        boolean isCompleted = (duration > 0 && currentTime >= duration * 0.95);

        learningRecordService.reportProgress(studentId, resourceId, currentTime, duration, isCompleted);
        return "OK";
    }
}
