package cn.dreamtof.core.config;


import cn.dreamtof.core.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    /**
     * 将 core 模块中预配置好的静态 ObjectMapper 注入到 Spring 容器中。
     * <p>
     * 这样做的好处：
     * 1. 彻底实现 "Single Source of Truth" (单点事实)，业务接口返回的 JSON 和日志里的 JSON 规则 100% 一致。
     * 2. 完美解决初始化时序问题：LogWriter 这种不归 Spring 管的类可以直接调 JsonUtils.MAPPER，
     * 而 Spring MVC、RedisSerializer 也可以通过注入这个 Bean 来使用它。
     * </p>
     *
     * @return 预配置好的全局静态 ObjectMapper 实例
     */
    @Bean
    @Primary // 强制 Spring MVC 等组件优先使用我们这个 Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.MAPPER;
    }
}