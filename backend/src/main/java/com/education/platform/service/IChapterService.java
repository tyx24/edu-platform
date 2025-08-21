package com.education.platform.service;

import com.education.platform.entity.Chapter;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 章节表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IChapterService extends IService<Chapter> {

    List<Chapter> getChapterTree(Long courseId); // 获取章节树
}
