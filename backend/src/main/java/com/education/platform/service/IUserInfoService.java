package com.education.platform.service;

import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-22
 */
public interface IUserInfoService extends IService<UserInfo> {

    PageResult<UserInfo> getUserList(PageRequest request);
}
