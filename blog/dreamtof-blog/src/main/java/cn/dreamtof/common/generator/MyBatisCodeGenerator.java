package cn.dreamtof.common.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.config.PackageConfig;
import com.mybatisflex.codegen.config.StrategyConfig;
import com.mybatisflex.codegen.config.TemplateConfig;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 👑 DDD 严格四层架构生成器 (模块-表映射批量生成版)
 * <p>
 * 核心特性：
 * 1. 模块-表映射配置：一个模块对应多张表，一次运行批量生成
 * 2. 多数据源支持：支持 PG(主库) 和 ClickHouse(日志库)
 * 3. 四层架构生成：Domain / Application / Infrastructure / API
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 在 MODULE_CONFIGS 中配置模块和表映射
 * new ModuleConfig("auth", "认证模块", "master", "public")
 *     .withTables("user", "role", "permission")
 * </pre>
 */
public class MyBatisCodeGenerator {

    // ==========================================
    // ⚙️ 全局基础配置
    // ==========================================
    private static final String BASE_PACKAGE_PREFIX = "cn.dreamtof";
    // 定义相对根目录的子模块路径
    private static final String MODULE_NAME = "blog/dreamtof-blog";

    // ==========================================
    // 🚀 模块-表映射配置 (核心配置区)
    // ==========================================
    // 在这里配置需要生成的模块，每个模块对应一个数据源和一组表
    // 格式：new ModuleConfig(模块名, 模块中文名, 数据源别名, schema).withTables(表名...)
    private static final List<ModuleConfig> MODULE_CONFIGS = Arrays.asList(
            // 认证模块 - 使用主库
            new ModuleConfig("auth", "认证", "master", "public")
                    .withTables("user"),


            // 歌曲模块 - 使用主库
            new ModuleConfig("song", "歌曲", "master", "public")
                    .withTables("song", "song_label", "song_click_stat","song_click_log","playlist", "playlist_song","label"),

            // 歌单模块 - 使用主库
//            new ModuleConfig("playlist", "歌单", "master", "public")
//                    .withTables("playlist", "playlist_song"),
//
//            // 标签模块 - 使用主库
//            new ModuleConfig("labels", "标签", "master", "public")
//                    .withTables("label"),

            // 日志模块 - 使用 ClickHouse
//            new ModuleConfig("log", "日志", "clickhouse", null)
//                    .withTables("app_logs", "operation_logs"),
            null
    );

    private static  final List<ModuleConfig>  BLOG_MODER =
  Arrays.asList(
                // ========== 1. Content 模块 — 文章、标签、分类、归档、日记 ==========
                new ModuleConfig("content", "内容管理", "master", "public")
                        .withTables("posts", "tags", "post_tags", "categories", "archives",
                                "diary_entries"),

                // ========== 2. Device 模块 — 设备 ==========
                new ModuleConfig("device", "设备", "master", "public")
                        .withTables("device_categories", "devices"),

                // ========== 3. Media 模块 — 相册、番剧、音乐 ==========
                new ModuleConfig("media", "媒体", "master", "public")
                        .withTables("albums", "album_photos", "anime", "music_playlists", "music_tracks"),

                // ========== 4. Portfolio 模块 — 项目、技能、时间线 ==========
                new ModuleConfig("portfolio", "作品集", "master", "public")
                        .withTables(
                                "projects", "project_tech_stacks", "project_tags",
                                "skills",
                                "timeline_events", "timeline_event_skills",
                                "timeline_event_achievements", "timeline_event_links"
                        ),

                // ========== 5. Social 模块 — 友链、评论 ==========
                new ModuleConfig("social", "社交", "master", "public")
                        .withTables("friends", "friend_tags", "friend_tag_links", "comments"),

                // ========== 6. System 模块 — 站点配置、页面、文件、导航、横幅、主题、统计 ==========
                new ModuleConfig("system", "系统管理", "master", "public")
                        .withTables("site_configs", "custom_pages", "uploaded_files",
                                "nav_links", "banners", "theme_settings",
                                "page_views", "daily_stats", "search_logs", "announcements"),
                                null
        );

    // ==========================================
    // 🗂️ 数据源别名常量
    // ==========================================
    private static final String DB_ALIAS_MASTER = "master";
    private static final String DB_ALIAS_CLICKHOUSE = "clickhouse";

    // 缓存数据源，避免重复创建
    private static final Map<String, DataSource> DATA_SOURCE_CACHE = new HashMap<>();

    public static void main(String[] args) {
        registerTypeMappings();

        System.out.println(">>> =========================================");
        System.out.println(">>> 🚀 DDD 四层架构代码生成器 (多线程并发版)");
        System.out.println(">>> =========================================");

        int moduleCount = BLOG_MODER.size()-1;
        String projectPath = getProjectPath();
        for (int i = 0; i < moduleCount; i++) {
            final int index = i;
            final ModuleConfig module = BLOG_MODER.get(index);
            // 2. 初始化数据源（注意：该方法内部必须实现线程安全）
            DataSource dataSource = getOrCreateDataSource(module.getDbAlias());
                String threadName = Thread.currentThread().getName();
                try {
                    System.out.printf("[%s] >>> 📦 正在生成: %s (%s)%n",
                            threadName, module.getModuleName(), module.getModuleCnName());
                    // 执行生成逻辑
                    generateModule(module, dataSource, projectPath);

                    System.out.printf("[%s] >>> ✅ 模块 [%s] 生成完成！%n",
                            threadName, module.getModuleName());
                } catch (Exception e) {
                    System.err.printf("[%s] >>> ❌ 模块 [%s] 生成失败: %s%n",
                            threadName, module.getModuleName(), e.getMessage());
                    e.printStackTrace();
                }
            }


        System.out.println("\n>>> =========================================");
        System.out.println(">>> 🎉 所有模块生成任务处理完毕！");
        System.out.println(">>> =========================================");
    }

    /**
     * 为单个模块执行四阶段代码生成
     * 💡 修复：强制在包名中加入 .blog 层级，确保代码生成在正确的业务包下
     */
    private static void generateModule(ModuleConfig module, DataSource dataSource, String projectPath) {
        // 修正：在 PREFIX 后强制拼接 .blog，使最终包名为 cn.dreamtof.blog.[moduleName]
        String basePackage = BASE_PACKAGE_PREFIX +"."+ module.getModuleName();

        // 1. Infra
        runGenerator(dataSource, createInfraPersistenceConfig(projectPath, basePackage, module), module.getDbAlias());
        // 2. App & Api & Domain Entity
        runGenerator(dataSource, createAppApiConfig(projectPath, basePackage, module), module.getDbAlias());
        // 3. Repository
        runGenerator(dataSource, createRepositoryConfig(projectPath, basePackage, module), module.getDbAlias());
        // 4. DTOs
        runGenerator(dataSource, createApiDtoConfig(projectPath, basePackage, module), module.getDbAlias());
    }

    /**
     * 💡 修复：针对 C:/code/blog/Mizuki 层级结构的路径探测逻辑
     */
    private static String getProjectPath() {
        String userDir = System.getProperty("user.dir").replaceAll("\\\\", "/");


        // 情况 A：如果你已经在 dreamtof-blog 目录里运行
        if (userDir.endsWith(MODULE_NAME) || userDir.endsWith("dreamtof-blog")) {
            // 如果是在 blog 目录下但不在 dreamtof-blog 里，需补全
            return userDir.endsWith(MODULE_NAME) ? userDir : userDir + "/dreamtof-blog";
        }

        // 情况 B：如果你在 Mizuki 根目录运行，拼接出完整的子模块物理路径
        File subModuleDir = new File(userDir, MODULE_NAME);
        if (subModuleDir.exists() && subModuleDir.isDirectory()) {
            return subModuleDir.getAbsolutePath().replaceAll("\\\\", "/");
        }

        // 兜底方案：直接返回当前目录（开发者需手动确认运行位置）
        return userDir;
    }

    /**
     * 🧠 运行生成器核心逻辑
     */
    private static void runGenerator(DataSource dataSource, GlobalConfig config, String dbAlias) {
        if (DB_ALIAS_MASTER.equals(dbAlias)) {
            new Generator(dataSource, config, IDialect.POSTGRESQL).generate();
        } else {
            new Generator(dataSource, config).generate();
        }
    }

    /**
     * 注册 JDBC 类型映射
     */
    private static void registerTypeMappings() {
        JdbcTypeMapping.registerMapping(Date.class, OffsetDateTime.class);
        JdbcTypeMapping.registerMapping(Timestamp.class, OffsetDateTime.class);
        JdbcTypeMapping.registerMapping(java.math.BigInteger.class, Long.class);
        JdbcTypeMapping.registerMapping(UUID.class, UUID.class);
    }

    /**
     * 获取或创建数据源（带缓存）
     */
    private static DataSource getOrCreateDataSource(String alias) {
        return DATA_SOURCE_CACHE.computeIfAbsent(alias, MyBatisCodeGenerator::initDataSource);
    }

    /**
     * 💡 修复：更健壮的项目路径检测逻辑
     */


    // ==========================================
    // 🛠️ 各阶段配置方法
    // ==========================================

    /**
     * [第一阶段] 配置 Infra 持久化层 (PO类, Mapper)
     */
    public static GlobalConfig createInfraPersistenceConfig(String projectPath, String basePackage, ModuleConfig module) {
        GlobalConfig config = new GlobalConfig();
        StrategyConfig strategy = config.getStrategyConfig();
        strategy.setGenerateTable(module.getTables());

        if (DB_ALIAS_MASTER.equals(module.getDbAlias()) && module.getSchema() != null) {
            strategy.setGenerateSchema(module.getSchema());
        }

        config.enableEntity()
                .setOverwriteEnable(true)
                .setWithLombok(true)
                .setWithSwagger(true)
                .setJdkVersion(21);
        config.getEntityConfig().setClassSuffix("PO");
        config.enableTableDef().setClassSuffix("TableDef");
        config.enableMapper().setClassSuffix("Mapper");
        config.enableMapperXml();

        config.disableService();
        config.disableServiceImpl();
        config.disableController();

        PackageConfig pc = config.getPackageConfig();
        pc.setBasePackage(basePackage);
        pc.setSourceDir(projectPath + "/src/main/java");
        pc.setMapperXmlPath(projectPath + "/src/main/resources/mapper");
        pc.setEntityPackage(basePackage + ".infrastructure.persistence.po");
        pc.setMapperPackage(basePackage + ".infrastructure.persistence.mapper");

        TemplateConfig tc = config.getTemplateConfig();
        tc.setEntity("templates/entity.tql");
        tc.setMapper("templates/mapper.tql");

        return config;
    }

    /**
     * [第二阶段] 配置 Application 服务、Domain Entity 与 API Controller
     */
    public static GlobalConfig createAppApiConfig(String projectPath, String basePackage, ModuleConfig module) {
        GlobalConfig config = new GlobalConfig();
        StrategyConfig strategy = config.getStrategyConfig();

        strategy.setGenerateTable(module.getTables());
        strategy.setLogicDeleteColumn("deleted");
        strategy.setVersionColumn("version");

        if (DB_ALIAS_MASTER.equals(module.getDbAlias()) && module.getSchema() != null) {
            strategy.setGenerateSchema(module.getSchema());
        }

        // 自定义配置：模块中文名和 URL 前缀
        config.setCustomConfig(Map.of(
                "tagPrefix", module.getModuleCnName(),
                "urlPrefix", module.getModuleName()
        ));

        // 1. 命名规范配置
        config.enableService();
        config.getServiceConfig().setClassSuffix("AppService");
        config.disableServiceImpl();

        config.enableController();
        config.getControllerConfig().setClassSuffix("Controller");

        // 生成领域实体 (无后缀)
        config.enableEntity().setWithLombok(true).setWithSwagger(true).setJdkVersion(21);
        config.getEntityConfig().setClassSuffix("");

        // 劫持 Mapper 槽位生成 Assembler
        config.enableMapper();
        config.getMapperConfig().setClassSuffix("Assembler");

        // 2. 包路由逻辑
        PackageConfig pc = config.getPackageConfig();
        pc.setBasePackage(basePackage);
        pc.setSourceDir(projectPath + "/src/main/java");

        pc.setEntityPackage(basePackage + ".domain.model.entity");
        pc.setMapperPackage(basePackage + ".application.assembler");
        pc.setServicePackage(basePackage + ".application.service");
        pc.setControllerPackage(basePackage + ".api.controller");

        // 3. 模板绑定
        TemplateConfig tc = config.getTemplateConfig();
        tc.setEntity("templates/domain_entity.tql");
        tc.setMapper("templates/assembler.tql");
        tc.setService("templates/app.tql");
        tc.setController("templates/controller.tql");

        config.disableMapperXml();
        config.getJavadocConfig().setAuthor("dream").setSince("");

        return config;
    }

    /**
     * [第三阶段] 配置 Domain Repository 接口与 Infrastructure Repository 实现
     */
    public static GlobalConfig createRepositoryConfig(String projectPath, String basePackage, ModuleConfig module) {
        GlobalConfig config = new GlobalConfig();
        StrategyConfig strategy = config.getStrategyConfig();
        strategy.setGenerateTable(module.getTables());

        if (DB_ALIAS_MASTER.equals(module.getDbAlias()) && module.getSchema() != null) {
            strategy.setGenerateSchema(module.getSchema());
        }

        // 设置主实体关联为领域实体 (无后缀)
        config.getEntityConfig()
                .setClassSuffix("")
                .setWithLombok(true)
                .setJdkVersion(21);

        config.getMapperConfig().setClassSuffix("Mapper");

        // 2. 命名规范
        config.enableService().setClassSuffix("Repository");
        config.enableServiceImpl().setClassSuffix("RepositoryImpl");

        // 3. 包路径配置
        PackageConfig pc = config.getPackageConfig();
        pc.setBasePackage(basePackage);
        pc.setSourceDir(projectPath + "/src/main/java");

        pc.setEntityPackage(basePackage + ".infrastructure.persistence.po");
        pc.setMapperPackage(basePackage + ".infrastructure.persistence.mapper");
        pc.setServicePackage(basePackage + ".domain.repository");
        pc.setServiceImplPackage(basePackage + ".infrastructure.persistence.repository");

        // 4. 模板配置
        TemplateConfig tc = config.getTemplateConfig();
        tc.setService("templates/repository.tql");
        tc.setServiceImpl("templates/repositoryImpl.tql");

        config.disableEntity();
        config.disableMapper();
        config.disableMapperXml();
        config.disableController();

        return config;
    }

    /**
     * [第四阶段] 配置 API 层的请求对象 (PageReq, CursorReq)
     */
    public static GlobalConfig createApiDtoConfig(String projectPath, String basePackage, ModuleConfig module) {
        GlobalConfig config = new GlobalConfig();
        StrategyConfig strategy = config.getStrategyConfig();
        strategy.setGenerateTable(module.getTables());

        if (DB_ALIAS_MASTER.equals(module.getDbAlias()) && module.getSchema() != null) {
            strategy.setGenerateSchema(module.getSchema());
        }

        config.enableService();
        config.getServiceConfig().setClassSuffix("PageReq");
        config.enableController();
        config.getControllerConfig().setClassSuffix("CursorReq");

        PackageConfig pc = config.getPackageConfig();
        pc.setBasePackage(basePackage);
        pc.setSourceDir(projectPath + "/src/main/java");

        pc.setServicePackage(basePackage + ".api.request");
        pc.setControllerPackage(basePackage + ".api.request");

        TemplateConfig tc = config.getTemplateConfig();
        tc.setService("templates/page_req.tql");
        tc.setController("templates/cursor_req.tql");

        config.disableServiceImpl();
        config.disableMapper();
        config.disableMapperXml();
        config.disableTableDef();
        config.disableEntity();

        return config;
    }

    /**
     * 🔌 数据源初始化逻辑
     */
    private static DataSource initDataSource(String alias) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            InputStream inputStream = MyBatisCodeGenerator.class.getClassLoader()
                    .getResourceAsStream("application-local.yaml");
            if (inputStream == null) {
                inputStream = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("application-local.yaml");
            }
            if (inputStream == null) {
                throw new RuntimeException("🚨 未找到配置文件: application-local.yaml");
            }

            Map<String, Object> yamlRoot = mapper.readValue(inputStream, Map.class);
            Map<String, Object> mybatisFlex = (Map<String, Object>) yamlRoot.get("mybatis-flex");
            Map<String, Object> datasource = (mybatisFlex != null) ? (Map<String, Object>) mybatisFlex.get("datasource") : null;
            Map<String, Object> targetDbConfig = (datasource != null) ? (Map<String, Object>) datasource.get(alias) : null;

            if (targetDbConfig == null) {
                throw new RuntimeException("🚨 未找到数据源配置: " + alias);
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(String.valueOf(targetDbConfig.get("driver-class-name")));
            config.setJdbcUrl(String.valueOf(targetDbConfig.get("url")));
            config.setUsername(String.valueOf(targetDbConfig.get("username")));
            config.setPassword(String.valueOf(targetDbConfig.get("password")));
            config.setMaximumPoolSize(5);

            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException("🚨 数据源初始化遇到致命错误: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // 📦 模块配置内部类
    // ==========================================

    /**
     * 模块配置类
     * 用于定义一个模块的生成配置：模块名、中文名、数据源、表列表
     */
    public static class ModuleConfig {
        /** 模块名（英文，用于包名） */
        private final String moduleName;
        /** 模块中文名（用于 Swagger Tag） */
        private final String moduleCnName;
        /** 数据源别名（master/clickhouse） */
        private final String dbAlias;
        /** 数据库 schema（PG 需要，ClickHouse 可为 null） */
        private final String schema;
        /** 该模块包含的表名列表 */
        private final List<String> tables = new ArrayList<>();

        public ModuleConfig(String moduleName, String moduleCnName, String dbAlias, String schema) {
            this.moduleName = moduleName;
            this.moduleCnName = moduleCnName;
            this.dbAlias = dbAlias;
            this.schema = schema;
        }

        /**
         * 批量添加表
         */
        public ModuleConfig withTables(String... tableNames) {
            this.tables.addAll(Arrays.asList(tableNames));
            return this;
        }

        // Getters
        public String getModuleName() {
            return moduleName;
        }

        public String getModuleCnName() {
            return moduleCnName;
        }

        public String getDbAlias() {
            return dbAlias;
        }

        public String getSchema() {
            return schema;
        }

        public String[] getTables() {
            return tables.toArray(new String[0]);
        }
    }
}
