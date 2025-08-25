package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.Course;

import com.education.platform.mapper.CourseMapper;
import com.education.platform.service.ICourseCommentService;
import com.education.platform.service.ICourseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.education.platform.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 课程表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private ICourseCommentService commentService;

    // 获取待审核的课程列表
    @Override
    public void approveCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new RuntimeException("课程不存在");

        course.setState((byte) 1); // 已发布
        course.setAuditTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

    // 驳回课程
    @Override
    public void rejectCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new RuntimeException("课程不存在");

        course.setState((byte) 2); // 已关闭/驳回
        course.setAuditTime(LocalDateTime.now());
        courseMapper.updateById(course);
    }

//    // 获取已发布的课程列表
//    @Override
//    public List<Course> listPublishedCourses() {
//        return courseMapper.selectList(new QueryWrapper<Course>().eq("state", 1)); // 已发布
//    }

    // 获取课程详情
    @Override
    public Map<String, Object> getCourseDetail(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }

        Map<String, Object> stats = commentService.getCourseCommentStats(courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("course", course);
        result.put("avgRating", stats.get("avgRating"));
        result.put("totalComments", stats.get("totalComments"));
        result.put("comments", commentService.getCommentsByCourseId(courseId));

        return result;
    }


    // 获取课程列表
    @Override
    public PageResult<Course> getCourseList(PageRequest request) {
        Page<Course> page = PageUtils.buildPage(request);

        LambdaQueryWrapper<Course> query = new LambdaQueryWrapper<>();

        // 根据关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            query.like(Course::getTitle, request.getKeyword())
                    .or().like(Course::getDescription, request.getKeyword());
        }
        // 状态筛选（已发布、待审核、关闭）
        if (request.getState() != null) {
            query.eq(Course::getState, request.getState());
        }

        // 类别筛选
        if (request.getCategory() != null) {
            query.eq(Course::getCategory, request.getCategory());
        }

        query.orderByDesc(Course::getAuditTime);

        Page<Course> result = courseMapper.selectPage(page, query);

        return new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );
    }

}
