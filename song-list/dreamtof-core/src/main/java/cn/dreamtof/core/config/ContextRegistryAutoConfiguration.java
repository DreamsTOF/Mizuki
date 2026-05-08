package cn.dreamtof.core.config;

import cn.dreamtof.core.context.ContextRegistry;
import cn.dreamtof.core.context.ContextRegistryProperties;
import cn.dreamtof.core.context.CurrentOperatorSupplier;
import cn.dreamtof.core.context.ThreadContextCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 上下文注册自动配置类
 * <p>
 * 支持两种注册模式（互斥）：
 * <ol>
 *   <li><b>Spring Bean 自动注入</b>（默认）：自动注入所有 {@link ThreadContextCopier} Bean</li>
 *   <li><b>注册表模式</b>：配置 dreamtof.context.registry.enabled=true，使用 ContextRegistry 显式注册</li>
 * </ol>
 * </p>
 */
@Configuration
public class ContextRegistryAutoConfiguration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ContextRegistryAutoConfiguration.class);

    @Value("${dreamtof.context.registry.enabled:false}")
    private boolean registryEnabled;

    @Autowired(required = false)
    private List<ThreadContextCopier> autoInjectCopiers;

    @Autowired(required = false)
    private List<CurrentOperatorSupplier> suppliers;

    @Override
    public void afterPropertiesSet() {
        ContextRegistry.enableRegistryMode(registryEnabled);
        
        if (registryEnabled) {
            // 注册表模式：不自动注册 ThreadContextCopier，等待用户手动调用 ContextRegistry.registerCopier()
            log.info("ContextRegistry: 注册表模式已启用，Spring Bean 自动注入将被忽略");
            
            if (suppliers != null && !suppliers.isEmpty()) {
                suppliers.forEach(ContextRegistry::registerOperatorSupplier);
                log.info("ContextRegistry: 已注册 {} 个 CurrentOperatorSupplier", suppliers.size());
            }
        } else {
            // 自动注入模式：将所有 ThreadContextCopier Bean 注册到 ContextRegistry
            log.info("ContextRegistry: Spring Bean 自动注入模式启用");
            
            if (autoInjectCopiers != null && !autoInjectCopiers.isEmpty()) {
                for (ThreadContextCopier copier : autoInjectCopiers) {
                    ContextRegistry.registerCopier(copier);
                    log.info("ContextRegistry: 自动注入并注册 [{}]", copier.getClass().getName());
                }
                log.info("ContextRegistry: 共自动注入 {} 个 ThreadContextCopier", autoInjectCopiers.size());
            } else {
                log.warn("ContextRegistry: 未发现任何 ThreadContextCopier Bean，请检查@Component扫描");
            }
        }
    }

    @Bean
    public ContextRegistryProperties contextRegistryProperties() {
        ContextRegistryProperties props = new ContextRegistryProperties();
        props.setEnabled(registryEnabled);
        return props;
    }
}
