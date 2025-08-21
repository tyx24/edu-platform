package com.education.platform.service;

import com.education.platform.dto.CourseCommentRequest;
import com.education.platform.entity.CourseComment;

import java.util.List;
import java.util.Map;

public interface ICourseCommentService {

    void addComment(Long studentId, CourseCommentRequest request);

    List<CourseComment> getCommentsByCourseId(Long courseId);

    /** 获取课程评论统计信息（平均分 + 评论总数） */
    Map<String, Object> getCourseCommentStats(Long courseId);
}
