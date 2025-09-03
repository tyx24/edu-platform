package com.education.platform.util;

/**
 * Redis缓存常量类
 * 定义缓存的key前缀和过期时间
 */
public class CacheConstants {

    /**
     * 用户相关缓存
     */
    public static final String USER_PREFIX = "user:";
    public static final String USER_TOKEN_PREFIX = "user:token:";
    public static final String USER_INFO_PREFIX = "user:info:";
    public static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    
    /**
     * 课程相关缓存
     */
    public static final String COURSE_PREFIX = "course:";
    public static final String COURSE_INFO_PREFIX = "course:info:";
    public static final String COURSE_LIST_PREFIX = "course:list:";
    public static final String COURSE_CHAPTERS_PREFIX = "course:chapters:";
    
    /**
     * 章节相关缓存
     */
    public static final String CHAPTER_PREFIX = "chapter:";
    public static final String CHAPTER_INFO_PREFIX = "chapter:info:";
    public static final String CHAPTER_RESOURCES_PREFIX = "chapter:resources:";
    
    /**
     * 考试相关缓存
     */
    public static final String EXAM_PREFIX = "exam:";
    public static final String EXAM_INFO_PREFIX = "exam:info:";
    public static final String EXAM_QUESTIONS_PREFIX = "exam:questions:";
    
    /**
     * 作业相关缓存
     */
    public static final String HOMEWORK_PREFIX = "homework:";
    public static final String HOMEWORK_INFO_PREFIX = "homework:info:";
    
    /**
     * 学习记录相关缓存
     */
    public static final String LEARNING_RECORD_PREFIX = "learning:record:";
    
    /**
     * 评论相关缓存
     */
    public static final String COMMENT_PREFIX = "comment:";
    public static final String COURSE_COMMENTS_PREFIX = "course:comments:";
    
    /**
     * 统计数据缓存
     */
    public static final String STATS_PREFIX = "stats:";
    public static final String USER_STATS_PREFIX = "stats:user:";
    public static final String COURSE_STATS_PREFIX = "stats:course:";
    
    /**
     * 验证码相关缓存
     */
    public static final String CAPTCHA_PREFIX = "captcha:";
    public static final String SMS_CODE_PREFIX = "sms:code:";
    public static final String EMAIL_CODE_PREFIX = "email:code:";
    
    /**
     * 过期时间常量（秒）
     */
    public static final long EXPIRE_TIME_30_MINUTES = 30 * 60; // 30分钟
    public static final long EXPIRE_TIME_1_HOUR = 60 * 60; // 1小时
    public static final long EXPIRE_TIME_2_HOURS = 2 * 60 * 60; // 2小时
    public static final long EXPIRE_TIME_6_HOURS = 6 * 60 * 60; // 6小时
    public static final long EXPIRE_TIME_12_HOURS = 12 * 60 * 60; // 12小时
    public static final long EXPIRE_TIME_1_DAY = 24 * 60 * 60; // 1天
    public static final long EXPIRE_TIME_3_DAYS = 3 * 24 * 60 * 60; // 3天
    public static final long EXPIRE_TIME_7_DAYS = 7 * 24 * 60 * 60; // 7天
    public static final long EXPIRE_TIME_30_DAYS = 30 * 24 * 60 * 60; // 30天
    
    /**
     * JWT Token过期时间
     */
    public static final long JWT_TOKEN_EXPIRE_TIME = EXPIRE_TIME_1_DAY; // JWT Token 1天过期
    
    /**
     * 验证码过期时间
     */
    public static final long CAPTCHA_EXPIRE_TIME = 5 * 60; // 验证码5分钟过期
    public static final long SMS_CODE_EXPIRE_TIME = 5 * 60; // 短信验证码5分钟过期
    public static final long EMAIL_CODE_EXPIRE_TIME = 10 * 60; // 邮箱验证码10分钟过期
    
    /**
     * 用户信息缓存过期时间
     */
    public static final long USER_INFO_EXPIRE_TIME = EXPIRE_TIME_1_HOUR; // 用户信息1小时过期
    
    /**
     * 课程信息缓存过期时间
     */
    public static final long COURSE_INFO_EXPIRE_TIME = EXPIRE_TIME_6_HOURS; // 课程信息6小时过期
    
    /**
     * 章节信息缓存过期时间
     */
    public static final long CHAPTER_INFO_EXPIRE_TIME = EXPIRE_TIME_2_HOURS; // 章节信息2小时过期
    
    /**
     * 考试信息缓存过期时间
     */
    public static final long EXAM_INFO_EXPIRE_TIME = EXPIRE_TIME_1_HOUR; // 考试信息1小时过期
    
    /**
     * 统计数据缓存过期时间
     */
    public static final long STATS_EXPIRE_TIME = EXPIRE_TIME_30_MINUTES; // 统计数据30分钟过期
}