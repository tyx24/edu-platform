package com.education.platform.service.impl;

import com.education.platform.dto.CourseCommentRequest;
import com.education.platform.entity.CourseComment;
import com.education.platform.mapper.CourseCommentMapper;
import com.education.platform.service.ICourseCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CourseCommentServiceImpl implements ICourseCommentService {

    @Autowired
    private CourseCommentMapper commentMapper;

    @Override
    public void addComment(Long studentId, CourseCommentRequest request) {
        CourseComment comment = new CourseComment();
        comment.setCourseId(request.getCourseId());
        comment.setStudentId(studentId);
        comment.setRating(request.getRating());
        comment.setContent(request.getContent());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
    }

    @Override
    public List<CourseComment> getCommentsByCourseId(Long courseId) {
        return commentMapper.findByCourseId(courseId);
    }

    @Override
    public Map<String, Object> getCourseCommentStats(Long courseId) {
        return commentMapper.getCourseCommentStats(courseId);
    }
}
