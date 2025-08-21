package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.platform.entity.Question;
import com.education.platform.mapper.QuestionMapper;
import com.education.platform.service.IQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试题目表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public void createQuestion(Question question) {
        questionMapper.insert(question);
    }

    @Override
    public void updateQuestion(Question question) {
        questionMapper.updateById(question);
    }

    @Override
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }

    @Override
    public List<Question> getQuestionsByExamId(Long examId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>().eq(Question::getExamId, examId)
        );
    }

    @Override
    public List<Question> getQuestionsForStudent(Long examId) {
        // 学生获取题目时，不能返回正确答案
        return getQuestionsByExamId(examId).stream().map(q -> {
            Question copy = new Question();
            copy.setId(q.getId());
            copy.setExamId(q.getExamId());
            copy.setType(q.getType());
            copy.setText(q.getText());
            copy.setOptions(q.getOptions());
            copy.setScore(q.getScore());
            return copy;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Question> listByExamId(Long examId) {
        return this.list(new LambdaQueryWrapper<Question>()
                .eq(Question::getExamId, examId)
                .orderByAsc(Question::getId));
    }
}
