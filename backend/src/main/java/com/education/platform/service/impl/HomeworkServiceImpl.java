package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Homework;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.HomeworkMapper;
import com.education.platform.service.IHomeworkService;
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
 * 作业表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements IHomeworkService {

    @Autowired
    private HomeworkMapper homeworkMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private RedisUtils redisUtils;

    private void checkTeacherOwnsCourse(Long courseId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long teacherId= Long.parseLong(auth.getName());
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("无权操作该课程");
        }
    }

    @Override
    public void createHomework(Homework homework) {
        checkTeacherOwnsCourse(homework.getCourseId());
        homeworkMapper.insert(homework);
        
        // 清除课程作业列表缓存
        clearHomeworkListCache(homework.getCourseId());
    }

    @Override
    public void updateHomework(Homework homework) {
        checkTeacherOwnsCourse(homework.getCourseId());
        homeworkMapper.updateById(homework);
        
        // 清除作业相关缓存
        clearHomeworkCache(homework.getId());
        clearHomeworkListCache(homework.getCourseId());
    }

    @Override
    public void deleteHomework(Long id) {
        Homework hw = homeworkMapper.selectById(id);
        if (hw != null) {
            checkTeacherOwnsCourse(hw.getCourseId());
            homeworkMapper.deleteById(id);
            
            // 清除作业相关缓存
            clearHomeworkCache(id);
            clearHomeworkListCache(hw.getCourseId());
        }
    }

    @Override
    public List<Homework> listByCourse(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.HOMEWORK_INFO_PREFIX + "course:" + courseId;
        List<Homework> cachedHomeworks = (List<Homework>) redisUtils.get(cacheKey);
        if (cachedHomeworks != null) {
            return cachedHomeworks;
        }
        
        List<Homework> homeworks = homeworkMapper.selectList(Wrappers.<Homework>lambdaQuery()
                .eq(Homework::getCourseId, courseId));
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, homeworks, CacheConstants.EXPIRE_TIME_1_HOUR);
        
        return homeworks;
    }
    
    /**
     * 获取单个作业信息（带缓存）
     */
    public Homework getHomeworkWithCache(Long homeworkId) {
        String cacheKey = CacheConstants.HOMEWORK_INFO_PREFIX + homeworkId;
        Homework cachedHomework = (Homework) redisUtils.get(cacheKey);
        if (cachedHomework != null) {
            return cachedHomework;
        }
        
        Homework homework = getById(homeworkId);
        if (homework != null) {
            redisUtils.set(cacheKey, homework, CacheConstants.EXPIRE_TIME_1_HOUR);
        }
        
        return homework;
    }
    
    /**
     * 清除单个作业缓存
     */
    private void clearHomeworkCache(Long homeworkId) {
        String cacheKey = CacheConstants.HOMEWORK_INFO_PREFIX + homeworkId;
        redisUtils.del(cacheKey);
    }
    
    /**
     * 清除课程作业列表缓存
     */
    private void clearHomeworkListCache(Long courseId) {
        String cacheKey = CacheConstants.HOMEWORK_INFO_PREFIX + "course:" + courseId;
        redisUtils.del(cacheKey);
    }
}
