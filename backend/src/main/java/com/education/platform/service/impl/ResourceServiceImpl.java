package com.education.platform.service.impl;

import com.education.platform.entity.Resource;
import com.education.platform.mapper.ResourceMapper;
import com.education.platform.service.IResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 资源表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceService {
    @Override
    public List<Resource> getResourcesByChapter(Long chapterId) {
        return lambdaQuery().eq(Resource::getChapterId, chapterId).list();
    }
}
