package com.education.platform.service.impl;

import com.education.platform.dto.CourseCommentRequest;
import com.education.platform.entity.CourseComment;
import com.education.platform.mapper.CourseCommentMapper;
import com.education.platform.service.ICourseCommentService;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CourseCommentServiceImpl implements ICourseCommentService {

    @Autowired
    private CourseCommentMapper commentMapper;
    
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void addComment(Long studentId, CourseCommentRequest request) {
        CourseComment comment = new CourseComment();
        comment.setCourseId(request.getCourseId());
        comment.setStudentId(studentId);
        comment.setRating(request.getRating());
        comment.setContent(request.getContent());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        
        // 清除课程评论相关缓存
        clearCourseCommentCache(request.getCourseId());
    }

    @Override
    public List<CourseComment> getCommentsByCourseId(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.COURSE_COMMENTS_PREFIX + courseId;
        List<CourseComment> cachedComments = (List<CourseComment>) redisUtils.get(cacheKey);
        if (cachedComments != null) {
            return cachedComments;
        }
        
        List<CourseComment> comments = commentMapper.findByCourseId(courseId);
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, comments, CacheConstants.EXPIRE_TIME_1_HOUR);
        
        return comments;
    }

    @Override
    public Map<String, Object> getCourseCommentStats(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.COURSE_COMMENTS_PREFIX + "stats:" + courseId;
        Map<String, Object> cachedStats = (Map<String, Object>) redisUtils.get(cacheKey);
        if (cachedStats != null) {
            return cachedStats;
        }
        
        Map<String, Object> stats = commentMapper.getCourseCommentStats(courseId);
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, stats, CacheConstants.EXPIRE_TIME_1_HOUR);
        
        return stats;
    }
    
    /**
     * 清除课程评论缓存
     */
    private void clearCourseCommentCache(Long courseId) {
        String commentsCacheKey = CacheConstants.COURSE_COMMENTS_PREFIX + courseId;
        String statsCacheKey = CacheConstants.COURSE_COMMENTS_PREFIX + "stats:" + courseId;
        redisUtils.del(commentsCacheKey, statsCacheKey);
    }
}
