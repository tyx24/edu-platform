package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.education.platform.entity.Chapter;
import com.education.platform.mapper.ChapterMapper;
import com.education.platform.service.IChapterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    /**
     * 获取课程章节树
     */
    public List<Chapter> getChapterTree(Long courseId) {
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
        return buildTree(childrenMap, 0L);
    }

    private List<Chapter> buildTree(Map<Long, List<Chapter>> childrenMap, Long parentId) {
        return childrenMap.getOrDefault(parentId, Collections.emptyList())
                .stream()
                .peek(ch -> ch.setChildren(buildTree(childrenMap, ch.getId())))
                .collect(Collectors.toList());
    }
}
