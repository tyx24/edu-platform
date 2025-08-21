package com.education.platform.service;

import com.education.platform.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 考试题目表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IQuestionService extends IService<Question> {
    void createQuestion(Question question);

    void updateQuestion(Question question);

    void deleteQuestion(Long id);

    List<Question> getQuestionsByExamId(Long examId);

    List<Question> listByExamId(Long examId);

    List<Question> getQuestionsForStudent(Long examId);
}
