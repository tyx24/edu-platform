package com.education.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 资源表
 * </p>
 *
 * @author tan
 * @since 2025-08-27
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
     * 资源标题（如：课件1、案例代码等）
     */
    private String title;

    /**
     * 资源类型（video/pdf/ppt/image/other）
     */
    private String type;

    /**
     * 文件访问地址（OSS、本地URL）
     */
    private String url;

    /**
     * 视频时长（秒，仅视频资源有值）
     */
    private Integer duration;

    /**
     * 上传时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
