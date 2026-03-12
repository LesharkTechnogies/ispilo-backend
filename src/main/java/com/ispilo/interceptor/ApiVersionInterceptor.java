package com.ispilo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1")) {
            response.setHeader("X-API-Version", "v1");
            response.setHeader("X-API-Deprecated", "true");
            response.setHeader("X-API-Upgrade-To", "v2");
            response.setHeader("X-API-Message", "v1 will be retired in a future release. Please upgrade to /api/v2.");
        } else if (path.startsWith("/api/v2")) {
            response.setHeader("X-API-Version", "v2");
        }
        return true;
    }
}
