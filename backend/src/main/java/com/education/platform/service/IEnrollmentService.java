package com.education.platform.service;

import com.education.platform.entity.Course;
import com.education.platform.entity.Enrollment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 选课表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IEnrollmentService extends IService<Enrollment> {
    void enroll(Long courseId, Long studentId);// 选课

    List<Course> getCoursesBystudentId(Long studentId); // 获取学生选课列表

//    void updateProgress(Long studentId, Long courseId, Byte progress); // 更新学习进度

    // 获取选课记录
    List<Enrollment> getEnrollments(Long student);

    void updateCourseProgress(Long studentId, Long courseId); // 更新课程学习进度

    List<Map<String, Object>> getEnrollmentsForExport(Long courseId); // 导出选课信息

    // 获取教师的所有课程及其学生
    List<Map<String, Object>> getMyCoursesWithstudents(Long teacherId); // 教师查询课程

    void dropCourse(Long courseId, Long studentId); // 退课
}
