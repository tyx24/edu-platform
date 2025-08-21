package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * 章节表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Chapter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 章节ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 排序
     */
    private Integer orderNum;

    /**
     * 父章节ID
     */
    private Long parentId;

    /**
     * 子章节列表
     */
    @TableField(exist = false)
    private List<Chapter> children = new ArrayList<>();
}
