package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.UserInfo;
import com.education.platform.mapper.UserInfoMapper;
import com.education.platform.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserInfoMapper UserInfoMapper;

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

        Page<UserInfo> result = UserInfoMapper.selectPage(page, query);

        return new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );
    }

    @Override
    public UserInfo getByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(UserInfo::getUserId, userId)
                .one();
    }
}
