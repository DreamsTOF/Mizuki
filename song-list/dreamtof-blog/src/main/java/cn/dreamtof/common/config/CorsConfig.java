package cn.dreamtof.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static cn.dreamtof.core.constants.GlobalConstants.TRACE_HEADER;

/**
 * 🌐 全局跨域资源共享 (CORS) 配置
 * 采用 Filter 方案，确保在所有业务逻辑（包括上下文初始化）之前处理 OPTIONS 请求。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 允许携带 Cookie 等凭证
        config.setAllowCredentials(true);

        // 2. 允许的源（生产环境建议配置具体域名）
        config.addAllowedOriginPattern("*");

        // 3. 允许的 HTTP 方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // 4. 允许的请求头
        // 除了标准头，显式放行你自定义的上下文 Header
        config.addAllowedHeader("*");

        // 5. 暴露给前端的响应头
        // 关键：必须暴露 satoken 和你的 TraceId，前端才能通过脚本读取到它们
        config.addExposedHeader("satoken");
        config.addExposedHeader(TRACE_HEADER);
        config.addExposedHeader("*");

        // 6. 预检请求有效期（单位：秒）
        // 设置为 1 小时，避免浏览器频繁发送 OPTIONS 请求，提升性能
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}