package com.education.platform.controller;

import com.education.platform.util.CacheConstants;
import com.education.platform.util.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis测试控制器
 * 用于测试Redis连接和基本功能
 */
@RestController
@RequestMapping("/redis")
@Tag(name = "Redis测试", description = "Redis连接和功能测试接口")
public class RedisTestController {

    @Autowired
    private RedisUtils redisUtils;

    @Operation(summary = "测试Redis连接", description = "测试Redis是否正常连接")
    @GetMapping("/test")
    public Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试字符串操作
            String testKey = "test:redis:connection";
            String testValue = "Redis连接测试成功 - " + System.currentTimeMillis();
            
            // 设置值
            boolean setResult = redisUtils.setString(testKey, testValue, 60);
            
            // 获取值
            String getValue = redisUtils.getString(testKey);
            
            // 检查是否存在
            boolean exists = redisUtils.hasKey(testKey);
            
            result.put("success", true);
            result.put("message", "Redis连接正常");
            result.put("setResult", setResult);
            result.put("setValue", testValue);
            result.put("getValue", getValue);
            result.put("exists", exists);
            result.put("valueMatch", testValue.equals(getValue));
            
            // 清理测试数据
            redisUtils.del(testKey);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Redis连接失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    @Operation(summary = "测试缓存操作", description = "测试各种缓存操作")
    @PostMapping("/cache-test")
    public Map<String, Object> testCacheOperations(@RequestBody Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String key = "test:cache:" + System.currentTimeMillis();
            
            // 测试对象缓存
            redisUtils.set(key, data, CacheConstants.EXPIRE_TIME_30_MINUTES);
            Object cachedData = redisUtils.get(key);
            
            // 测试Hash操作
            String hashKey = "test:hash:" + System.currentTimeMillis();
            redisUtils.hset(hashKey, "field1", "value1", CacheConstants.EXPIRE_TIME_30_MINUTES);
            redisUtils.hset(hashKey, "field2", "value2");
            Object hashValue = redisUtils.hget(hashKey, "field1");
            
            result.put("success", true);
            result.put("message", "缓存操作测试成功");
            result.put("originalData", data);
            result.put("cachedData", cachedData);
            result.put("hashValue", hashValue);
            
            // 清理测试数据
            redisUtils.del(key, hashKey);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "缓存操作测试失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    @Operation(summary = "获取Redis信息", description = "获取Redis基本信息")
    @GetMapping("/info")
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试基本操作
            String infoKey = "redis:info:test";
            redisUtils.setString(infoKey, "info", 10);
            
            long expire = redisUtils.getExpire(infoKey);
            boolean exists = redisUtils.hasKey(infoKey);
            
            result.put("success", true);
            result.put("message", "Redis信息获取成功");
            result.put("testKeyExists", exists);
            result.put("testKeyExpire", expire);
            Map<String, Long> constants = new HashMap<>();
            constants.put("JWT_TOKEN_EXPIRE", CacheConstants.JWT_TOKEN_EXPIRE_TIME);
            constants.put("USER_INFO_EXPIRE", CacheConstants.USER_INFO_EXPIRE_TIME);
            constants.put("COURSE_INFO_EXPIRE", CacheConstants.COURSE_INFO_EXPIRE_TIME);
            
            result.put("constants", constants);
            
            // 清理测试数据
            redisUtils.del(infoKey);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Redis信息获取失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }
}