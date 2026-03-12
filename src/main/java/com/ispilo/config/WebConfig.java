package com.ispilo.config;

import com.ispilo.interceptor.AuditInterceptor;
import com.ispilo.interceptor.ApiVersionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;
    private final ApiVersionInterceptor apiVersionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**") // Audit all API endpoints
                .excludePathPatterns("/api/auth/**"); // Optionally exclude auth if handled separately

    registry.addInterceptor(apiVersionInterceptor)
        .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "X-App-ID", "X-Device-ID")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from the local 'uploads' directory
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:uploads/");
    }
}
