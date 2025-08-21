package com.education.platform.service;

import com.education.platform.entity.ExamRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 考试记录表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IExamRecordService extends IService<ExamRecord> {

    void submitExam(ExamRecord record); // 提交考试

    List<ExamRecord> listByExam(Long examId); // 根据考试id获取考试记录

    List<ExamRecord> listMyExams(Long studentId); // 获取我的考试记录
}
