package com.education.platform.service;

import com.education.platform.entity.Exam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 考试表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IExamService extends IService<Exam> {
    void createExam(Exam exam); // 创建考试

    void updateExam(Exam exam); // 更新考试

    void deleteExam(Long id); // 删除考试

    List<Exam> listByCourse(Long courseId); // 根据课程id获取考试列表
}
