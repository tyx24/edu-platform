package com.education.platform.service;

import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.dto.PasswordDTO;
import com.education.platform.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.education.platform.util.R;

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
    
    void logout(Long userId);

    PageResult<User> getUserList(PageRequest request, String role);

    R<Object> changePassword(PasswordDTO dto);
}
