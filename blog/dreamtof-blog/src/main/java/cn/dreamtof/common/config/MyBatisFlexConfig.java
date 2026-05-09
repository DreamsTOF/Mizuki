package cn.dreamtof.common.config;


import cn.dreamtof.common.util.UserContextUtil;
import cn.dreamtof.core.base.*;
import cn.dreamtof.core.utils.TraceIdHandler;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.OffsetDateTime;

/**
 * MyBatis-Flex 全局配置
 */
@Configuration
@Order(-1)
public class MyBatisFlexConfig implements MyBatisFlexCustomizer {

    private static final String UUID_V7_GENERATOR = "uuidV7";

    static {
        KeyGeneratorFactory.register(UUID_V7_GENERATOR, new UuidV7Generator());
    }

    @Override
    public void customize(FlexGlobalConfig globalConfig) {

        System.out.println("===== MyBatisFlexConfig.customize() called =====");
        System.out.println("===== defaultConfig before: " + FlexGlobalConfig.getDefaultConfig().getKeyConfig());
        System.out.println("===== globalConfig before: " + globalConfig.getKeyConfig());

        // --- 1. 全局乐观锁配置 ---
        globalConfig.setVersionColumn("version");
        globalConfig.setDbType(DbType.POSTGRE_SQL);

        // --- 2. 全局主键生成配置 ---
        FlexGlobalConfig.KeyConfig keyConfig = new FlexGlobalConfig.KeyConfig();
        keyConfig.setKeyType(com.mybatisflex.annotation.KeyType.Generator);
        keyConfig.setValue(UUID_V7_GENERATOR);
        // 关键：必须设置到 defaultConfig，因为 IdInfo.initDefaultKeyType() 读取的是 getDefaultConfig()
        FlexGlobalConfig.getDefaultConfig().setKeyConfig(keyConfig);
        globalConfig.setKeyConfig(keyConfig);

        System.out.println("===== defaultConfig after: " + FlexGlobalConfig.getDefaultConfig().getKeyConfig());
        System.out.println("===== globalConfig after: " + globalConfig.getKeyConfig());
        System.out.println("===== keyConfig.keyType: " + keyConfig.getKeyType());
        System.out.println("===== keyConfig.value: " + keyConfig.getValue());

        // --- 3. 插入监听器 (Insert Listener) ---
        globalConfig.registerInsertListener(entity -> {
            OffsetDateTime now = OffsetDateTime.now();
            if (entity instanceof IdAudit audit) {
                if (audit.getId() == null) {
                    audit.setId(TraceIdHandler.createUUID());
                }
            }
            if (entity instanceof CreatedTimeAudit) {
                ((CreatedTimeAudit) entity).setCreatedAt(now);
            }
            if (entity instanceof UpdatedTimeAudit) {
                ((UpdatedTimeAudit) entity).setUpdatedAt(now);
            }
            if (entity instanceof VersionAudit) {
                ((VersionAudit) entity).setVersion(1);
            }
            // 分开判断创建人和更新人
            if (entity instanceof CreatedByAudit) {
                ((CreatedByAudit) entity).setCreatedBy(UserContextUtil.getCurrentUserIdOrDef());
            }
            if (entity instanceof UpdatedByAudit) {
                ((UpdatedByAudit) entity).setUpdatedBy(UserContextUtil.getCurrentUserIdOrDef());
            }
        }, Object.class);

        // --- 3. 更新监听器 (Update Listener) ---
        globalConfig.registerUpdateListener(entity -> {
            if (entity instanceof UpdatedTimeAudit) {
                ((UpdatedTimeAudit) entity).setUpdatedAt(OffsetDateTime.now());
            }
            if (entity instanceof UpdatedByAudit) {
                ((UpdatedByAudit) entity).setUpdatedBy(UserContextUtil.getCurrentUserIdOrDef());
            }
        }, Object.class);

    }

    /**
     * SQL 审计日志打印（开发环境使用）
     */
    @Bean
    public MessageCollector messageCollector() {
        // 开启审计功能
        AuditManager.setAuditEnable(true);
        return new ConsoleMessageCollector();
    }
}
