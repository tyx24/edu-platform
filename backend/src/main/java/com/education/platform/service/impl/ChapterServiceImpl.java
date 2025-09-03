package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Chapter;
import com.education.platform.mapper.ChapterMapper;
import com.education.platform.service.IChapterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 章节表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements IChapterService {

    @Autowired
    private ChapterMapper chapterMapper;
    
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 获取课程章节树
     */
    public List<Chapter> getChapterTree(Long courseId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.COURSE_CHAPTERS_PREFIX + courseId;
        List<Chapter> cachedChapters = (List<Chapter>) redisUtils.get(cacheKey);
        if (cachedChapters != null) {
            return cachedChapters;
        }
        
        // 一次性查出所有章节
        List<Chapter> allChapters = chapterMapper.selectList(
                Wrappers.<Chapter>lambdaQuery()
                        .eq(Chapter::getCourseId, courseId)
                        .orderByAsc(Chapter::getOrderNum)
        );

        // 按父ID分组
        Map<Long, List<Chapter>> childrenMap = allChapters.stream()
                .collect(Collectors.groupingBy(c -> Optional.ofNullable(c.getParentId()).orElse(0L)));

        // 递归构建
        List<Chapter> chapterTree = buildTree(childrenMap, 0L);
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, chapterTree, CacheConstants.CHAPTER_INFO_EXPIRE_TIME);
        
        return chapterTree;
    }

    private List<Chapter> buildTree(Map<Long, List<Chapter>> childrenMap, Long parentId) {
        return childrenMap.getOrDefault(parentId, Collections.emptyList())
                .stream()
                .peek(ch -> ch.setChildren(buildTree(childrenMap, ch.getId())))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean save(Chapter entity) {
        boolean result = super.save(entity);
        if (result && entity.getCourseId() != null) {
            // 清除课程章节缓存
            clearChapterCache(entity.getCourseId());
        }
        return result;
    }
    
    @Override
    public boolean updateById(Chapter entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getCourseId() != null) {
            // 清除课程章节缓存
            clearChapterCache(entity.getCourseId());
            // 清除单个章节缓存
            clearSingleChapterCache(entity.getId());
        }
        return result;
    }
    
    @Override
    public boolean removeById(Long id) {
        Chapter chapter = getById(id);
        boolean result = super.removeById(id);
        if (result && chapter != null && chapter.getCourseId() != null) {
            // 清除课程章节缓存
            clearChapterCache(chapter.getCourseId());
            // 清除单个章节缓存
            clearSingleChapterCache(id);
        }
        return result;
    }
    
    /**
     * 获取单个章节信息（带缓存）
     */
    public Chapter getChapterWithCache(Long chapterId) {
        String cacheKey = CacheConstants.CHAPTER_INFO_PREFIX + chapterId;
        Chapter cachedChapter = (Chapter) redisUtils.get(cacheKey);
        if (cachedChapter != null) {
            return cachedChapter;
        }
        
        Chapter chapter = getById(chapterId);
        if (chapter != null) {
            redisUtils.set(cacheKey, chapter, CacheConstants.CHAPTER_INFO_EXPIRE_TIME);
        }
        
        return chapter;
    }
    
    /**
     * 清除课程章节缓存
     */
    private void clearChapterCache(Long courseId) {
        String cacheKey = CacheConstants.COURSE_CHAPTERS_PREFIX + courseId;
        redisUtils.del(cacheKey);
    }
    
    /**
     * 清除单个章节缓存
     */
    private void clearSingleChapterCache(Long chapterId) {
        String cacheKey = CacheConstants.CHAPTER_INFO_PREFIX + chapterId;
        redisUtils.del(cacheKey);
    }
}
