package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 考试表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考试ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 标题
     */
    private String title;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 时长(分钟)
     */
    private Integer duration;
}
