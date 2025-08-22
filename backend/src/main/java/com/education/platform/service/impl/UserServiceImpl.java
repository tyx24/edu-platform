package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.entity.User;
import com.education.platform.mapper.UserMapper;
import com.education.platform.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.JwtUtils;
import com.education.platform.util.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    // 密码加密
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注册
    @Override
    public User register(String username, String password, String email, String role) {
        // 查重
        User existing = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existing != null) throw new RuntimeException("用户名已存在");

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        // 默认角色为学生，教师需审核
        if ("teacher".equals(role)) {
            user.setRole("teacher");
            user.setStatus((byte) 0); // 待审核
        } else {
            user.setRole("student");
            user.setStatus((byte) 1); // 正常
        }

        userMapper.insert(user);
        return user;
    }

    // 登录
    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

        // 用户不存在或密码错误
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户已被禁用");
        }
        return jwtUtils.generateToken(user.getId(), user.getRole());
    }

    // 获取用户列表
    @Override
    public PageResult<User> getUserList(PageRequest request, String role) {
        Page<User> page = PageUtils.buildPage(request);

        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();

        // 根据关键词搜索
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            query.like(User::getUsername, request.getKeyword())
                    .or().like(User::getEmail, request.getKeyword());
        }
        // 状态筛选（已发布、待审核、关闭）
        if (request.getState() != null) {
            System.out.println(query.eq(User::getStatus, request.getState()));
            query.eq(User::getStatus, request.getState());
        }
        // 角色筛选
        if (role != null && !role.isEmpty()) {
            System.out.println(query.eq(User::getRole, role));
            query.eq(User::getRole, role);
        }


        Page<User> result = userMapper.selectPage(page, query);

        return new PageResult<>(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                result.getRecords()
        );
    }
}
