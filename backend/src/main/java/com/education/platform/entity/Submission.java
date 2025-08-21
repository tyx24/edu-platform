package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * <p>
 * 作业提交表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 提交ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long homeworkId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 文件OSS地址
     */
    private String fileUrl;

    /**
     * 分数
     */
    private Integer score;

    /**
     * 评语
     */
    private String comment;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
}
