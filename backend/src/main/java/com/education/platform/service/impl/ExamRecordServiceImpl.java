package com.education.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Exam;
import com.education.platform.entity.ExamRecord;
import com.education.platform.entity.Question;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.ExamMapper;
import com.education.platform.mapper.ExamRecordMapper;
import com.education.platform.service.IExamRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.service.IExamService;
import com.education.platform.service.IQuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 考试记录表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements IExamRecordService {

    @Autowired
    private IExamService examService;
    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private IQuestionService questionService;


    @Override
    public void submitExam(ExamRecord record) {
        // 1. 获取考试信息
        Exam exam = examService.getById(record.getExamId());
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }

        // 2. 考试时间校验
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(exam.getStartTime())) {
            throw new RuntimeException("考试尚未开始");
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束，无法提交");
        }

        // 3. 切屏警告次数限制（比如超过3次直接0分）
        final int MAX_WARNING = 3;
        if (record.getWarningCount() > MAX_WARNING) {
            record.setTotalScore(0);
            // 保存记录
            this.save(record);
            return;
        }

        // 4. 自动判分逻辑
        List<Question> questions = questionService.listByExamId(record.getExamId());
        Map<Long, String> studentAnswers = JSON.parseObject(record.getAnswers(), new TypeReference<Map<Long, String>>() {
        });

        int totalScore = 0;

        for (Question q : questions) {
            String correctAnswer = q.getAnswer().trim();
            String studentAnswer = studentAnswers.getOrDefault(q.getId(), "").trim();

            switch (q.getType()) {
                case "single":
                case "judge":
                    // 单选 & 判断：完全一致才得分
                    if (studentAnswer.equalsIgnoreCase(correctAnswer)) {
                        totalScore += q.getScore();
                    }
                    break;

                case "multi":
                    // 多选题判分优化
                    Set<String> correctSet = new HashSet<>(Arrays.asList(correctAnswer.split(",")));
                    Set<String> studentSet = new HashSet<>(Arrays.asList(studentAnswer.split(",")));

                    if (studentSet.equals(correctSet)) {
                        // 完全正确
                        totalScore += q.getScore();
                    } else if (correctSet.containsAll(studentSet) && !studentSet.isEmpty()) {
                        // 部分正确（按比例给分）
                        double ratio = (double) studentSet.size() / correctSet.size();
                        totalScore += (int) Math.round(q.getScore() * ratio);
                    } else {
                        // 选错或多选
                        totalScore += 0;
                    }
                    break;
            }
        }

        record.setTotalScore(totalScore);
        record.setSubmitTime(now);
        this.save(record);

    }

    @Override
    public List<ExamRecord> listByExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        Course course = courseMapper.selectById(exam.getCourseId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long Id = Long.parseLong(auth.getName());
        if (!course.getTeacherId().equals(Id)) {
            throw new AccessDeniedException("无权查看考试成绩");
        }
        return examRecordMapper.selectList(Wrappers.<ExamRecord>lambdaQuery()
                .eq(ExamRecord::getExamId, examId));
    }

    @Override
    public List<ExamRecord> listMyExams(Long studentId) {
        return examRecordMapper.selectList(Wrappers.<ExamRecord>lambdaQuery()
                .eq(ExamRecord::getStudentId, studentId));
    }
}
