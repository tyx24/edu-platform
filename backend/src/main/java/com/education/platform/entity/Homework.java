package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 作业表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Homework implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 作业ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 作业标题
     */
    private String title;

    /**
     * 作业描述
     */
    private String description;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;
}
