package com.education.platform.security;

import com.education.platform.util.CacheConstants;
import com.education.platform.util.JwtUtils;
import com.education.platform.util.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

// JWT 认证过滤器
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    
    @Autowired
    private RedisUtils redisUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtils.validateToken(token)) {
                Long userId = jwtUtils.extractUserId(token);
                String role = jwtUtils.extractRole(token);
                
                // 检查 Redis 中是否存在此 Token
                String tokenKey = CacheConstants.USER_TOKEN_PREFIX + userId;
                String cachedToken = redisUtils.getString(tokenKey);
                
                // 只有当 Redis 中存在且匹配的 Token 才视为有效
                if (cachedToken != null && cachedToken.equals(token)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singleton(() -> role)
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 将认证信息存入 SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);

//                    System.out.println("JWT 验证通过，用户ID: " + userId + "，角色: " + role);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
