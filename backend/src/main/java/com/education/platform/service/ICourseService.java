package com.education.platform.service;

import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.Course;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 课程表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface ICourseService extends IService<Course> {

    void approveCourse(Long courseId);   // 管理员审核通过

    void rejectCourse(Long courseId);    // 管理员驳回

//    List<Course> listPublishedCourses(); // 学生查询课程

    /**
     * 获取课程详情（包含评论统计和评论列表）
     */
    Map<String, Object> getCourseDetail(Long courseId); // 获取课程详情

    PageResult<Course> getCourseList(PageRequest request, String difficulty); // 分页查询课程

}
