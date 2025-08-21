package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;

/**
 * <p>
 * 考试题目表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 类型 single/multi/judge
     */
    private String type;

    /**
     * 题干
     */
    private String text;

    /**
     * 选项 JSON
     */
    private String options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 分值
     */
    private Integer score;
}
