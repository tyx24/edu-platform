package com.education.platform.dto;

import lombok.Data;

@Data
public class PageRequest {
    /** 当前页码（默认1） */
    private Integer pageNum = 1;

    /** 每页数量（默认10） */
    private Integer pageSize = 10;

    /** 搜索关键词（模糊匹配） */
    private String keyword;

    /** 状态筛选（例如：已发布、待审核等） */
    private Integer state;

    /** 类别筛选 */
    private String category;
}

