package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 选课表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Enrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选课ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 选课时间
     */
    private LocalDateTime enrollTime;

    /**
     * 学习进度
     */
    private Byte progress;
}
