package cn.dreamtof.audit.core;

import cn.dreamtof.audit.utils.SmartTransactionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class AuditTransactionConfig {

    @Bean
    @Primary // 【核心】确保业务模块注入时优先选择审计版的 Template
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new SmartTransactionTemplate(transactionManager);
    }

    @Bean("readOnlyTransactionTemplate")
    public TransactionTemplate readOnlyTransactionTemplate(PlatformTransactionManager transactionManager) {
        SmartTransactionTemplate template = new SmartTransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template;
    }
}