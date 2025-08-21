package com.education.platform.service;

import com.education.platform.entity.Resource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 资源表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IResourceService extends IService<Resource> {

    List<Resource> getResourcesByChapter(Long chapterId); // 根据章节获取资源
}
