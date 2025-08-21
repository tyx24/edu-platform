package com.education.platform.service;

import com.education.platform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
public interface IUserService extends IService<User> {
    User register(String username, String password, String email, String role);

    String login(String username, String password);
}
