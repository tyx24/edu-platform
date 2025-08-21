//package com.education.platform.security;
//
//import com.education.platform.util.JwtUtils;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//// 拦截器，用于验证用户是否登录
//
//@Component
//public class LoginInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private JwtUtils jwtUtils;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // 获取请求头中的Authorization字段
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//
//        String token = authHeader.substring(7); // Remove "Bearer "
//
//        if (!jwtUtils.validateToken(token)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return false;
//        }
//
//        // 解析用户信息并存入上下文
//        Long userId = jwtUtils.extractUserId(token);
//        String role = jwtUtils.extractRole(token);
//        UserContext.set(userId, role);
//
//        // 打印日志
//        System.out.println("JWT验证通过，用户ID: " + userId + ", 角色: " + role);
//
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        UserContext.clear(); // 防止线程复用导致信息泄露
//    }
//}
