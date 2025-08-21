package com.education.platform.service;

import com.education.platform.entity.Submission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 作业提交表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface ISubmissionService extends IService<Submission> {
    void submitHomework(Submission submission); // 提交作业

    void gradeSubmission(Long submissionId, Integer score, String comment); // 评分作业

    List<Submission> listByHomework(Long homeworkId); // 根据作业ID查询提交记录

    List<Submission> listMySubmissions(Long studentId); // 根据学生ID查询提交记录
}
