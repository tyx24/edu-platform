package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * 资源表
 * </p>
 *
 * @author tan
 * @since 2025-08-03
 */
@Data
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 资源类型 video/pdf/ppt
     */
    private String type;

    /**
     * OSS文件地址
     */
    private String url;

    /**
     * 视频时长(秒)
     */
    private Integer duration;

    /**
     * 资源标题（文件名/显示名）
     */
    @TableField(exist = false)
    private String title;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private LocalDateTime createTime;
}
