package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Exam;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.ExamMapper;
import com.education.platform.service.IExamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.RedisUtils;
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
    @Autowired
    private RedisUtils redisUtils;

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
        
        // 清除课程考试列表缓存
        clearExamListCache(exam.getCourseId());
    }

    @Override
    public void updateExam(Exam exam) {
        checkTeacherOwnsCourse(exam.getCourseId());
        examMapper.updateById(exam);
        
        // 清除考试相关缓存
        clearExamCache(exam.getId());
        clearExamListCache(exam.getCourseId());
    }

    @Override
    public void deleteExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam != null) {
            checkTeacherOwnsCourse(exam.getCourseId());
            examMapper.deleteById(id);
            
            // 清除考试相关缓存
            clearExamCache(id);
            clearExamListCache(exam.getCourseId());
        }
    }

    @Override
    public List<Exam> listByCourse(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.EXAM_INFO_PREFIX + "course:" + courseId;
        List<Exam> cachedExams = (List<Exam>) redisUtils.get(cacheKey);
        if (cachedExams != null) {
            return cachedExams;
        }
        
        List<Exam> exams = examMapper.selectList(Wrappers.<Exam>lambdaQuery()
                .eq(Exam::getCourseId, courseId));
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, exams, CacheConstants.EXAM_INFO_EXPIRE_TIME);
        
        return exams;
    }
    
    /**
     * 获取单个考试信息（带缓存）
     */
    public Exam getExamWithCache(Long examId) {
        String cacheKey = CacheConstants.EXAM_INFO_PREFIX + examId;
        Exam cachedExam = (Exam) redisUtils.get(cacheKey);
        if (cachedExam != null) {
            return cachedExam;
        }
        
        Exam exam = getById(examId);
        if (exam != null) {
            redisUtils.set(cacheKey, exam, CacheConstants.EXAM_INFO_EXPIRE_TIME);
        }
        
        return exam;
    }
    
    /**
     * 清除单个考试缓存
     */
    private void clearExamCache(Long examId) {
        String cacheKey = CacheConstants.EXAM_INFO_PREFIX + examId;
        redisUtils.del(cacheKey);
    }
    
    /**
     * 清除课程考试列表缓存
     */
    private void clearExamListCache(Long courseId) {
        String cacheKey = CacheConstants.EXAM_INFO_PREFIX + "course:" + courseId;
        redisUtils.del(cacheKey);
    }
}
