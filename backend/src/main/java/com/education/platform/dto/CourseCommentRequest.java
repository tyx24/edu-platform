package com.education.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提交课程评论请求")
public class CourseCommentRequest {

    @Schema(description = "课程ID", example = "101")
    private Long courseId;

    @Schema(description = "评分 1-5", example = "5")
    private Byte rating;

    @Schema(description = "评论内容", example = "课程讲得很清楚，收获很大！")
    private String content;
}