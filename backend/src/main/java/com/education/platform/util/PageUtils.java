package com.education.platform.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.platform.dto.PageRequest;

public class PageUtils {
    public static <T> Page<T> buildPage(PageRequest request) {
        return new Page<>(request.getPageNum(), request.getPageSize());
    }
}

