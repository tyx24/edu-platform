package com.education.platform.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();

        // 设置数据库类型（可选，但推荐指定）
        paginationInnerInterceptor.setDbType(DbType.MYSQL); // 根据实际数据库调整

        // 设置分页溢出处理（请求页数超过总页数时）
        paginationInnerInterceptor.setOverflow(true); // true: 回到首页 false:继续请求

        // 设置单页最大条数限制（可选）
        paginationInnerInterceptor.setMaxLimit(500L); // 防止恶意传入超大size

        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}