package com.education.platform.service.impl;

import com.education.platform.entity.Resource;
import com.education.platform.mapper.ResourceMapper;
import com.education.platform.service.IResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    @Autowired
    private RedisUtils redisUtils;
    
    @Override
    public List<Resource> getResourcesByChapter(Long chapterId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.CHAPTER_RESOURCES_PREFIX + chapterId;
        List<Resource> cachedResources = (List<Resource>) redisUtils.get(cacheKey);
        if (cachedResources != null) {
            return cachedResources;
        }
        
        List<Resource> resources = lambdaQuery().eq(Resource::getChapterId, chapterId).list();
        
        // 存储到 Redis 缓存
        redisUtils.set(cacheKey, resources, CacheConstants.EXPIRE_TIME_2_HOURS);
        
        return resources;
    }
    
    @Override
    public boolean save(Resource entity) {
        boolean result = super.save(entity);
        if (result && entity.getChapterId() != null) {
            // 清除章节资源缓存
            clearChapterResourcesCache(entity.getChapterId());
        }
        return result;
    }
    
    @Override
    public boolean updateById(Resource entity) {
        boolean result = super.updateById(entity);
        if (result && entity.getChapterId() != null) {
            // 清除章节资源缓存
            clearChapterResourcesCache(entity.getChapterId());
        }
        return result;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        Resource resource = getById(id);
        boolean result = super.removeById(id);
        if (result && resource != null && resource.getChapterId() != null) {
            // 清除章节资源缓存
            clearChapterResourcesCache(resource.getChapterId());
        }
        return result;
    }
    
    /**
     * 清除章节资源缓存
     */
    private void clearChapterResourcesCache(Long chapterId) {
        String cacheKey = CacheConstants.CHAPTER_RESOURCES_PREFIX + chapterId;
        redisUtils.del(cacheKey);
    }
}
