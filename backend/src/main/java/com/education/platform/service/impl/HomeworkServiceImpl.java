package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Course;
import com.education.platform.entity.Homework;
import com.education.platform.mapper.CourseMapper;
import com.education.platform.mapper.HomeworkMapper;
import com.education.platform.service.IHomeworkService;
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
    }

    @Override
    public void updateHomework(Homework homework) {
        checkTeacherOwnsCourse(homework.getCourseId());
        homeworkMapper.updateById(homework);
    }

    @Override
    public void deleteHomework(Long id) {
        Homework hw = homeworkMapper.selectById(id);
        if (hw != null) {
            checkTeacherOwnsCourse(hw.getCourseId());
            homeworkMapper.deleteById(id);
        }
    }

    @Override
    public List<Homework> listByCourse(Long courseId) {
        return homeworkMapper.selectList(Wrappers.<Homework>lambdaQuery()
                .eq(Homework::getCourseId, courseId));
    }
}
