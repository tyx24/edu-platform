package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author tan
 * @since 2025-08-11
 */
@Data
@TableName("learning_record")
public class LearningRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 当前观看时间（秒）
     */
    @TableField("`current_time`")
    private Integer currentTime;

    /**
     * 视频总时长（秒）
     */
    private Integer duration;

    /**
     * 是否完成（0否 1是）
     */
    private Boolean completed;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}