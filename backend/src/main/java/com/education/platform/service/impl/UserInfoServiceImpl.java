package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.BasicInfoDTO;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.User;
import com.education.platform.entity.UserInfo;
import com.education.platform.mapper.UserInfoMapper;
import com.education.platform.mapper.UserMapper;
import com.education.platform.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.PageUtils;
import com.education.platform.util.R;
import com.education.platform.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-22
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public PageResult<UserInfo> getUserList(PageRequest request) {
        Page<UserInfo> page = PageUtils.buildPage(request);

        LambdaQueryWrapper<UserInfo> query = new LambdaQueryWrapper<>();

        // 根据关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            query.like(UserInfo::getRealName, request.getKeyword())  // 姓名
                    .or().like(UserInfo::getMajor, request.getKeyword()) // 专业
                    .or().like(UserInfo::getStudentId, request.getKeyword()); // 学号
        }

        query.orderByDesc(UserInfo::getId);

        Page<UserInfo> result = userInfoMapper.selectPage(page, query);

        return new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );
    }

    @Override
    public UserInfo getByUserId(Long userId) {
        // 先从 Redis 缓存中获取
        String cacheKey = CacheConstants.USER_INFO_PREFIX + "detail:" + userId;
        UserInfo cachedUserInfo = (UserInfo) redisUtils.get(cacheKey);
        if (cachedUserInfo != null) {
            return cachedUserInfo;
        }
        
        UserInfo userInfo = this.lambdaQuery()
                .eq(UserInfo::getUserId, userId)
                .one();
        
        if (userInfo != null) {
            // 存储到 Redis 缓存
            redisUtils.set(cacheKey, userInfo, CacheConstants.USER_INFO_EXPIRE_TIME);
        }
        
        return userInfo;
    }

    @Override
    public R<Object> updateBasicInfo(BasicInfoDTO request) {

        Long userId = getUserId();
        // 1. 更新 user_info 表里的 real_name
        userInfoMapper.update(null,
                new UpdateWrapper<UserInfo>()
                        .eq("user_id", userId)
                        .set("real_name", request.getRealName())
        );

        // 2. 更新 user 表里的 email
        userMapper.update(null,
                new UpdateWrapper<User>()
                        .eq("id", userId)
                        .set("email", request.getEmail())
        );
        
        // 清除用户信息缓存
        clearUserInfoCache(userId);

        return R.ok();
    }

    @Override
    public R<Object> updateContactInfo(UserInfo request) {

        Long userId = getUserId();
        userInfoMapper.update(null,
                new UpdateWrapper<UserInfo>()
                        .eq("user_id", userId)
                        .set("phone", request.getPhone())
                        .set("qq", request.getQq())
                        .set("wechat", request.getWechat())
        );
        
        // 清除用户信息缓存
        clearUserInfoCache(userId);
        
        return R.ok();
    }

    @Override
    public R<Object> updateStudentInfo(UserInfo request) {

        Long userId = getUserId();
        userInfoMapper.update(null,
                new UpdateWrapper<UserInfo>()
                        .eq("user_id", userId)
                        .set("student_id", request.getStudentId())
                        .set("department", request.getDepartment())
                        .set("major", request.getMajor())
                        .set("grade", request.getGrade())
        );
        
        // 清除用户信息缓存
        clearUserInfoCache(userId);
        
        return R.ok();
    }

    @Override
    public R<Object> updateTeacherInfo(UserInfo request) {

        Long userId = getUserId();
        userInfoMapper.update(null,
                new UpdateWrapper<UserInfo>()
                        .eq("user_id", userId)
                        .set("title", request.getTitle())
                        .set("department", request.getDepartment())
                        .set("research", request.getResearch())
        );
        
        // 清除用户信息缓存
        clearUserInfoCache(userId);
        
        return R.ok();
    }

    @Override
    public R<Object> updateBio(UserInfo request) {

        Long userId = getUserId();
        userInfoMapper.update(null,
                new UpdateWrapper<UserInfo>()
                        .eq("user_id", userId)
                        .set("bio", request.getBio())
        );
        
        // 清除用户信息缓存
        clearUserInfoCache(userId);
        
        return R.ok();
    }

    // 获取当前id
    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        long userId;
        try {
            userId = Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("当前用户信息异常");
        }
        return userId;
    }
    
    /**
     * 清除用户信息缓存
     */
    private void clearUserInfoCache(Long userId) {
        String cacheKey = CacheConstants.USER_INFO_PREFIX + "detail:" + userId;
        redisUtils.del(cacheKey);
    }

}
