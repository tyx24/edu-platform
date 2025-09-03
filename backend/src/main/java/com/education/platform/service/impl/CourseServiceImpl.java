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

import com.education.platform.util.CacheConstants;
import com.education.platform.util.PageUtils;
import com.education.platform.util.RedisUtils;
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

    @Autowired
    private RedisUtils redisUtils;

    // 获取待审核的课程列表
    @Override
    public void approveCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new RuntimeException("课程不存在");

        course.setState((byte) 1); // 已发布
        course.setAuditTime(LocalDateTime.now());
        courseMapper.updateById(course);

        // 清除相关缓存
        clearCourseCache(courseId);
        clearCourseListCache();
    }

    // 驳回课程
    @Override
    public void rejectCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new RuntimeException("课程不存在");

        course.setState((byte) 2); // 已关闭/驳回
        course.setAuditTime(LocalDateTime.now());
        courseMapper.updateById(course);

        // 清除相关缓存
        clearCourseCache(courseId);
        clearCourseListCache();
    }

//    // 获取已发布的课程列表
//    @Override
//    public List<Course> listPublishedCourses() {
//        return courseMapper.selectList(new QueryWrapper<Course>().eq("state", 1)); // 已发布
//    }

    // 获取课程详情
    @Override
    public Map<String, Object> getCourseDetail(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.COURSE_INFO_PREFIX + courseId;
        Map<String, Object> cachedResult = (Map<String, Object>) redisUtils.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

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

        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, result, CacheConstants.COURSE_INFO_EXPIRE_TIME);

        return result;
    }


    // 获取课程列表
    @Override
    public PageResult<Course> getCourseList(PageRequest request, String difficulty) {
        // 生成缓存key，根据查询条件生成唯一key
        String cacheKey = generateCourseListCacheKey(request, difficulty);
        PageResult<Course> cachedResult = (PageResult<Course>) redisUtils.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

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

        // 难度筛选
        if (difficulty != null && !difficulty.isEmpty()) {
            query.eq(Course::getDifficulty, difficulty);
        }

        query.orderByDesc(Course::getAuditTime);

        Page<Course> result = courseMapper.selectPage(page, query);

        PageResult<Course> pageResult = new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );

        // 存储到 Redis 缓存，较短的过期时间因为列表数据变化频繁
        redisUtils.set(cacheKey, pageResult, CacheConstants.EXPIRE_TIME_30_MINUTES);

        return pageResult;
    }

    /**
     * 生成课程列表缓存key
     */
    private String generateCourseListCacheKey(PageRequest request, String difficulty) {
        StringBuilder key = new StringBuilder(CacheConstants.COURSE_LIST_PREFIX);
        key.append("page:").append(request.getPageNum());
        key.append(":size:").append(request.getPageSize());

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            key.append(":keyword:").append(request.getKeyword().hashCode());
        }
        if (request.getState() != null) {
            key.append(":state:").append(request.getState());
        }
        if (request.getCategory() != null) {
            key.append(":category:").append(request.getCategory());
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            key.append(":difficulty:").append(difficulty);
        }


        return key.toString();
    }

    /**
     * 清除课程缓存
     */
    private void clearCourseCache(Long courseId) {
        String cacheKey = CacheConstants.COURSE_INFO_PREFIX + courseId;
        redisUtils.del(cacheKey);
    }

    /**
     * 清除课程列表缓存（模糊删除）
     */
    private void clearCourseListCache() {
        // 清除所有课程列表缓存
        // 这里简化处理，实际项目中可以使用更精细的缓存策略
        // 可以考虑使用 Redis 的 scan 命令来查找匹配的 key
    }

}
