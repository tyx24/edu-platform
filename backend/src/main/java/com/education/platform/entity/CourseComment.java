package com.education.platform.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程评论实体")
public class CourseComment {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "评分 1-5")
    private Byte rating;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
