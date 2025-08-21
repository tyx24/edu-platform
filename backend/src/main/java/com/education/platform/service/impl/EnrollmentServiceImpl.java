package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Enrollment;
import com.education.platform.entity.User;
import com.education.platform.mapper.EnrollmentMapper;
import com.education.platform.service.ICourseService;
import com.education.platform.service.IEnrollmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.service.ILearningRecordService;
import com.education.platform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 选课表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class EnrollmentServiceImpl extends ServiceImpl<EnrollmentMapper, Enrollment> implements IEnrollmentService {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private IUserService userService;

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private ILearningRecordService learningRecordService;

    // 选课
    @Override
    public void enroll(Long courseId, Long studentId) {
        // 检查是否已选课
        boolean exists = this.count(new QueryWrapper<Enrollment>()
                .eq("course_id", courseId)
                .eq("student_id", studentId)) > 0;

        if (exists) {
            throw new RuntimeException("您已选过此课程");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setEnrollTime(LocalDateTime.now());
        enrollment.setProgress((byte) 0);

        this.save(enrollment);
    }

    // 退课
    @Override
    public void dropCourse(Long courseId, Long studentId) {
        boolean removed = lambdaUpdate()
                .eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStudentId, studentId)
                .remove();

        if (!removed) {
            throw new RuntimeException("未找到该课程的选课记录");
        }
    }


    // 根据学生id获取选课列表
    @Override
    public List<Course> getCoursesBystudentId(Long studentId) {
        List<Enrollment> enrollments = lambdaQuery()
                .eq(Enrollment::getStudentId, studentId).list();
        List<Long> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId).collect(Collectors.toList());
        return courseService.listByIds(courseIds);
    }


    // 获取选课记录
    @Override
    public List<Enrollment> getEnrollments(Long studentId) {
        return lambdaQuery().eq(Enrollment::getStudentId, studentId).list();
    }

    //    // 根据学生id和课程id更新学习进度
//    @Override
//    public void updateProgress(Long studentId, Long courseId, Byte progress) {
//        lambdaUpdate()
//                .eq(Enrollment::getstudentId, studentId)
//                .eq(Enrollment::getCourseId, courseId)
//                .set(Enrollment::getProgress, progress)
//                .update();
//    }
    // 根据学生id和课程id更新学习进度
    @Override
    public void updateCourseProgress(Long studentId, Long courseId) {
        double progress = learningRecordService.getCourseProgress(studentId, courseId);

        enrollmentMapper.update(
                null,
                Wrappers.<Enrollment>lambdaUpdate()
                        .eq(Enrollment::getStudentId, studentId)
                        .eq(Enrollment::getCourseId, courseId)
                        .set(Enrollment::getProgress, (byte) Math.round(progress))
        );
    }

    // 根据课程id导出选课列表
    @Override
    public List<Map<String, Object>> getEnrollmentsForExport(Long courseId) {
        List<Enrollment> enrollments = lambdaQuery().eq(Enrollment::getCourseId, courseId).list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Enrollment e : enrollments) {
            User student = userService.getById(e.getStudentId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", student.getId());
            map.put("username", student.getUsername());
            map.put("email", student.getEmail());
            map.put("progress", e.getProgress());
            result.add(map);
        }
        return result;
    }

    // 获取教师的所有课程及其学生
    @Override
    public List<Map<String, Object>> getMyCoursesWithstudents(Long teacherId) {
        List<Course> courses = courseService.lambdaQuery().eq(Course::getTeacherId, teacherId).list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Course course : courses) {
            List<Enrollment> enrollments = lambdaQuery().eq(Enrollment::getCourseId, course.getId()).list();

            List<User> students = new ArrayList<>();
            for (Enrollment enrollment : enrollments) {
                User student = userService.getById(enrollment.getStudentId());
                students.add(student);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("course", course);
            map.put("students", students);
            result.add(map);
        }

        return result;
    }
}
