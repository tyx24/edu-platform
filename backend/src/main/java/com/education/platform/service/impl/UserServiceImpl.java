package com.education.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;
import com.education.platform.dto.PageResult;
import com.education.platform.dto.PasswordDTO;
import com.education.platform.entity.User;
import com.education.platform.mapper.UserMapper;
import com.education.platform.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.platform.util.CacheConstants;
import com.education.platform.util.JwtUtils;
import com.education.platform.util.PageUtils;
import com.education.platform.util.R;
import com.education.platform.util.RedisUtils;
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

    @Autowired
    private RedisUtils redisUtils;

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
        
        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        
        // 将Token存储到Redis中，设置过期时间
        String tokenKey = CacheConstants.USER_TOKEN_PREFIX + user.getId();
        redisUtils.setString(tokenKey, token, CacheConstants.JWT_TOKEN_EXPIRE_TIME);
        
        // 缓存用户信息到Redis
        String userInfoKey = CacheConstants.USER_INFO_PREFIX + user.getId();
        redisUtils.set(userInfoKey, user, CacheConstants.USER_INFO_EXPIRE_TIME);
        
        return token;
    }
    
    // 退出登录
    @Override
    public void logout(Long userId) {
        // 从 Redis 中删除 Token 和用户信息
        String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
        String userInfoKey = CacheConstants.USER_INFO_PREFIX + userId;
        
        redisUtils.del(tokenKey, userInfoKey);
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
            query.eq(User::getStatus, request.getState());
        }
        // 角色筛选
        if (role != null && !role.isEmpty()) {
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

    @Override
    public R<Object> changePassword(PasswordDTO dto) {
        User user = userMapper.selectById(dto.getUserId());
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return R.fail("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        
        // 更新数据库
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            // 清除用户相关缓存，强制重新登录
            String tokenKey = CacheConstants.USER_TOKEN_PREFIX + dto.getUserId();
            String userInfoKey = CacheConstants.USER_INFO_PREFIX + dto.getUserId();
            redisUtils.del(tokenKey, userInfoKey);
        }
        
        return R.ok(result);
    }
    
    /**
     * 获取用户信息（带缓存）
     */
    public User getUserWithCache(Long userId) {
        String cacheKey = CacheConstants.USER_INFO_PREFIX + userId;
        User cachedUser = (User) redisUtils.get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        User user = getById(userId);
        if (user != null) {
            // 不缓存密码信息
            User cacheUser = new User();
            cacheUser.setId(user.getId());
            cacheUser.setUsername(user.getUsername());
            cacheUser.setEmail(user.getEmail());
            cacheUser.setRole(user.getRole());
            cacheUser.setStatus(user.getStatus());
            cacheUser.setCreateTime(user.getCreateTime());
            
            redisUtils.set(cacheKey, cacheUser, CacheConstants.USER_INFO_EXPIRE_TIME);
            return user; // 返回完整的用户信息
        }
        
        return null;
    }
    
    @Override
    public boolean updateById(User entity) {
        boolean result = super.updateById(entity);
        if (result) {
            // 清除用户信息缓存
            clearUserCache(entity.getId());
        }
        return result;
    }
    
    /**
     * 清除用户缓存
     */
    private void clearUserCache(Long userId) {
        String userInfoKey = CacheConstants.USER_INFO_PREFIX + userId;
        redisUtils.del(userInfoKey);
    }
}
