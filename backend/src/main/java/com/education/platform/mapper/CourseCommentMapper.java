package com.education.platform.mapper;

import com.education.platform.entity.CourseComment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseCommentMapper {

    @Insert("INSERT INTO course_comment(course_id, student_id, rating, content, create_time) " +
            "VALUES(#{courseId}, #{studentId}, #{rating}, #{content}, #{createTime})")
    void insert(CourseComment comment);

    @Select("SELECT * FROM course_comment WHERE course_id = #{courseId} ORDER BY create_time DESC")
    List<CourseComment> findByCourseId(Long courseId);

    /** 统计某课程的平均评分和评论总数 */
    @Select("SELECT AVG(rating) AS avgRating, COUNT(*) AS totalComments " +
            "FROM course_comment WHERE course_id = #{courseId}")
    Map<String, Object> getCourseCommentStats(Long courseId);
}
