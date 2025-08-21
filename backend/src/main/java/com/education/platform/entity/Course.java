package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 课程表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 教师ID
     */
    private Long teacherId;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 封面地址
     */
    private String coverUrl;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 0待审核 1已发布 2关闭
     */
    private Byte state;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}
