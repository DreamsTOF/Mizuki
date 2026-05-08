package cn.dreamtof.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/**",                  // Knife4j 文档页
            "/webjars/**",           // 静态资源
            "/v3/api-docs/**",       // OpenAPI 元数据
            "/favicon.ico",
            "/error",
            "/auth/register",        // 用户注册接口
            "/auth/login",           // 用户登录接口
            "/auth/refresh-token",   // 刷新Token接口
            "/api/host/**",          // 主播公开页面
            "/api/playlist/public/**", // 公开歌单（无需认证）
            "/api/song/list",        // 歌曲列表（条件认证）
            "/api/song/{id}",        // 歌曲详情（条件认证，GET）
            "/api/song/*/click",     // 点击记录（无需认证，POST）
            "/api/song/*/clicks",    // 点击统计（无需认证，GET）
            "/api/tag"               // 标签列表（公开，GET）
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/**")
                            .notMatch(EXCLUDE_PATHS)
                            .check(r -> StpUtil.checkLogin());
                })).addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
