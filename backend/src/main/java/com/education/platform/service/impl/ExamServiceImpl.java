package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Exam;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.ExamMapper;
import com.education.platform.service.IExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 考试表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    @Autowired
    private ExamMapper examMapper;
    @Autowired
    private CourseMapper courseMapper;

    private void checkTeacherOwnsCourse(Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long teacherId = Long.parseLong(auth.getName());
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("无权操作该课程");
        }
    }

    @Override
    public void createExam(Exam exam) {
        checkTeacherOwnsCourse(exam.getCourseId());
        examMapper.insert(exam);
    }

    @Override
    public void updateExam(Exam exam) {
        checkTeacherOwnsCourse(exam.getCourseId());
        examMapper.updateById(exam);
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam != null) {
            checkTeacherOwnsCourse(exam.getCourseId());
            examMapper.deleteById(id);
        }
    }

    @Override
    public List<Exam> listByCourse(Long courseId) {
        return examMapper.selectList(Wrappers.<Exam>lambdaQuery()
                .eq(Exam::getCourseId, courseId));
    }
}
