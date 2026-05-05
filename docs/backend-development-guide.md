# Mizuki 博客系统 - 后端开发指南

## 一、项目概述

### 1.1 系统定位

Mizuki 是一个面向个人博客的前后端分离系统，后端提供 RESTful API，前端负责渲染和交互。

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| **DDD 领域驱动** | 按限界上下文划分模块，模块内事务保证一致性，模块间通过领域事件解耦 |
| **UUID 主键** | 所有主键为 UUID，业务层生成，数据库不自增 |
| **无显式外键** | 不设置数据库外键约束，表间关系由业务层维护 |
| **乐观锁** | 所有表含 `version` 字段，并发更新时校验版本号 |
| **软删除** | 所有业务表含 `deleted_at` 字段，逻辑删除而非物理删除 |
| **JSONB 数组** | 数组类型统一使用 JSONB，比 TEXT[] 更灵活 |

---

## 二、技术栈

### 2.1 推荐技术选型

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **数据库** | PostgreSQL | 15+ | 主数据库，支持 JSONB、全文搜索、GIN 索引 |
| **后端框架** | Spring Boot | 3.x | Java 后端框架 |
| **ORM** | MyBatis-Plus | 3.5+ | 简化 CRUD，支持乐观锁插件 |
| **缓存** | Redis | 7.x | 会话缓存、热点数据缓存 |
| **消息队列** | 内置事件总线 | - | 模块间领域事件通信（可使用 Spring ApplicationEvent） |
| **对象存储** | 本地/OSS | - | 文件、图片存储 |
| **文档** | SpringDoc | 2.x | OpenAPI 3.0 文档 |

### 2.2 开发环境要求

```
JDK 17+
Maven 3.9+ / Gradle 8+
PostgreSQL 15+
Redis 7+
```

---

## 三、模块划分与依赖关系

### 3.1 模块总览（37 张表，8 个模块）

| 模块 | 表数量 | 包含表 | 聚合根 |
|------|--------|--------|--------|
| **Content** | 5 | posts, tags, post_tags, categories, archives | Post |
| **Portfolio** | 8 | projects, project_tech_stacks, project_tags, skills, timeline_events, timeline_event_skills, timeline_event_achievements, timeline_event_links | Project, Skill, TimelineEvent |
| **Social** | 4 | friends, friend_tags, friend_tag_links, comments | Friend, Comment |
| **Media** | 5 | albums, album_photos, anime, music_playlists, music_tracks | Album, Anime, MusicPlaylist |
| **Device** | 2 | device_categories, devices | DeviceCategory, Device |
| **System** | 7 | site_configs, custom_pages, nav_links, uploaded_files, banners, theme_settings | SiteConfig, CustomPage, NavLink |
| **Analytics** | 4 | page_views, daily_stats, search_logs, announcements | PageView, DailyStat, SearchLog, Announcement |
| **Diary** | 1 | diary_entries | DiaryEntry |

> **注**：`about_pages` 已合并到 `custom_pages`，通过 `page_key` 区分。

### 3.2 模块依赖关系图

```
                    ┌─────────────┐
                    │   System    │
                    │ (基础设施)  │
                    └──────┬──────┘
                           │ 被所有模块依赖
            ┌──────────────┼──────────────┐
            │              │              │
     ┌──────▼──────┐ ┌────▼────┐ ┌───────▼──────┐
     │   Content   │ │ Portfolio│ │    Device    │
     │ (文章/标签) │ │(项目/技能)│ │   (设备)     │
     └──────┬──────┘ └────┬────┘ └───────┬──────┘
            │              │              │
            │              │              │
     ┌──────▼──────┐ ┌────▼────┐ ┌───────▼──────┐
     │   Social    │ │  Media  │ │  Analytics   │
     │ (友链/评论) │ │(相册/音乐)│ │ (统计/公告)  │
     └─────────────┘ └─────────┘ └──────────────┘
            │
            ▼
     ┌─────────────┐
     │    Diary    │
     │   (日记)    │
     └─────────────┘
```

### 3.3 领域事件清单

| 事件 | 发布模块 | 订阅模块 | 说明 |
|------|---------|---------|------|
| `PostPublished` | Content | Analytics | 文章发布时，Analytics 记录发布事件 |
| `PostDeleted` | Content | Analytics | 文章删除时，Analytics 更新统计 |
| `CommentPosted` | Social | Content, Analytics | 评论发布时，Content 更新评论数，Analytics 记录 |
| `PhotoUploaded` | Media | System | 图片上传时，System 记录文件 |
| `FileUploaded` | System | - | 文件上传完成（被 Media 订阅） |

---

## 四、开发阶段规划

### 4.1 开发顺序原则

1. **先基础设施，后业务模块**：System 模块提供文件上传、配置管理等基础能力
2. **先独立模块，后依赖模块**：无外部依赖的模块优先开发
3. **先核心模块，后辅助模块**：Content 是博客核心，优先开发
4. **每个阶段可独立测试、独立部署**

### 4.2 阶段划分

```
Phase 1: 基础设施 (1-2 周)
    └── System 模块

Phase 2: 核心内容 (2-3 周)
    ├── Content 模块
    └── Diary 模块

Phase 3: 个人展示 (1-2 周)
    ├── Portfolio 模块
    └── Device 模块

Phase 4: 社交与媒体 (1-2 周)
    ├── Social 模块
    └── Media 模块

Phase 5: 数据分析 (1 周)
    └── Analytics 模块

Phase 6: 集成与优化 (1 周)
    ├── 事件总线集成
    ├── 缓存优化
    └── 性能测试
```

---

## 五、详细开发顺序

### Phase 1: 基础设施（第 1-2 周）

#### Step 1.1: 项目初始化

**任务**：搭建 Spring Boot 项目基础结构

**输出**：
- `pom.xml` / `build.gradle`
- 项目包结构
- 数据库连接配置
- MyBatis-Plus 配置

**包结构**：
```
src/main/java/com/mizuki/
├── system/                    # System 模块
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── entity/
│   └── dto/
├── content/                   # Content 模块
├── portfolio/                 # Portfolio 模块
├── social/                    # Social 模块
├── media/                     # Media 模块
├── device/                    # Device 模块
├── analytics/                 # Analytics 模块
└── diary/                     # Diary 模块
```

#### Step 1.2: System 模块开发

**任务**：开发基础设施配置管理

**开发顺序**：
1. `site_configs` - 站点配置 CRUD
2. `custom_pages` - 自定义页面 CRUD
3. `nav_links` - 导航链接 CRUD
4. `uploaded_files` - 文件上传功能
5. `banners` - 横幅图片 CRUD
6. `theme_settings` - 主题设置 CRUD

**优先开发理由**：文件上传能力被其他模块依赖，需优先开发。

---

### Phase 2: 核心内容（第 3-5 周）

#### Step 1.3: Content 模块开发

**任务**：开发文章、标签、分类、归档管理

**开发顺序**：
1. `tags` - 标签 CRUD（最基础）
2. `categories` - 分类 CRUD
3. `posts` - 文章 CRUD（核心）
4. `post_tags` - 文章-标签关联（跟随文章一起）
5. `archives` - 归档索引（监听文章事件自动生成）

**领域事件**：
- 文章发布 → 发布 `PostPublished` 事件
- 文章删除 → 发布 `PostDeleted` 事件

#### Step 1.4: Diary 模块开发

**任务**：开发日记管理

**开发顺序**：
1. `diary_entries` - 日记 CRUD
2. 图片上传（复用 System 模块的文件上传能力）

---

### Phase 3: 个人展示（第 4-5 周）

#### Step 3.1: Portfolio 模块开发

**任务**：开发项目、技能、时间线管理

**开发顺序**：
1. `skills` - 技能 CRUD
2. `projects` - 项目 CRUD
3. `project_tech_stacks` - 项目技术栈
4. `project_tags` - 项目标签
5. `timeline_events` - 时间线事件 CRUD
6. `timeline_event_skills` - 事件技能关联
7. `timeline_event_achievements` - 事件成就关联
8. `timeline_event_links` - 事件链接关联

**理由**：技能模块相对简单，优先开发建立信心。

#### Step 3.2: Device 模块开发

**任务**：开发设备管理

**开发顺序**：
1. `device_categories` - 设备分类 CRUD
2. `devices` - 设备 CRUD

**理由**：模块简单，1-2 天可完成。

---

### Phase 4: 社交与媒体（第 6-7 周）

#### Step 4.1: Social 模块开发

**任务**：开发友链、评论管理

**开发顺序**：
1. `friends` - 友链 CRUD
2. `friend_tags` - 友链标签 CRUD
3. `friend_tag_links` - 友链-标签关联
4. `comments` - 评论 CRUD

**领域事件**：
- 评论发布 → 发布 `CommentPosted` 事件
  - Content 模块订阅：更新文章评论数
  - Analytics 模块订阅：记录评论统计

#### Step 4.2: Media 模块开发

**任务**：开发相册、番剧、音乐管理

**开发顺序**：
1. `albums` - 相册 CRUD
2. `album_photos` - 相册图片管理
3. `anime` - 番剧 CRUD
4. `music_playlists` - 播放列表 CRUD
5. `music_tracks` - 曲目 CRUD

**领域事件**：
- 图片上传 → 发布 `PhotoUploaded` 事件
  - System 模块订阅：记录文件上传

---

### Phase 5: 数据分析（第 8 周）

#### Step 5.1: Analytics 模块开发

**任务**：开发访问统计、搜索记录、公告管理

**开发顺序**：
1. `announcements` - 公告 CRUD（相对简单）
2. `page_views` - 页面访问记录
3. `search_logs` - 搜索记录
4. `daily_stats` - 每日统计汇总（定时任务生成）

**订阅事件**：
- `PostPublished` → 记录发布事件
- `CommentPosted` → 更新评论统计
- `PostDeleted` → 更新统计

---

### Phase 6: 集成与优化（第 9 周）

#### Step 6.1: 事件总线集成

**任务**：确保所有模块间事件通信正常

**检查点**：
- [ ] `PostPublished` 事件 → Analytics 正确接收
- [ ] `CommentPosted` 事件 → Content 和 Analytics 正确接收
- [ ] `PhotoUploaded` 事件 → System 正确接收

#### Step 6.2: 缓存优化

**任务**：为热点数据添加缓存

**缓存策略**：
| 数据 | 缓存时间 | 缓存键 |
|------|---------|--------|
| 站点配置 | 1 小时 | `site:config:{key}` |
| 文章列表 | 10 分钟 | `content:posts:list:{page}` |
| 友链列表 | 30 分钟 | `social:friends:list` |
| 导航链接 | 1 小时 | `system:nav:{position}` |
| 主题设置 | 1 小时 | `system:theme:{key}` |

#### Step 6.3: 性能测试

**任务**：对核心接口进行性能测试

**测试指标**：
- 文章列表查询 < 100ms（P95）
- 文章详情查询 < 50ms（P95）
- 文件上传 < 500ms（1MB 图片）

---

## 六、开发规范

### 6.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| **包名** | 小写，模块名 | `com.mizuki.content.service` |
| **类名** | 大驼峰 | `PostService`, `PostMapper` |
| **方法名** | 小驼峰 | `getPostById`, `listPosts` |
| **常量** | 全大写，下划线 | `MAX_PAGE_SIZE` |
| **数据库表** | 小写，下划线 | `post_tags`, `device_categories` |
| **字段名** | 小写，下划线 | `has_draft`, `created_at` |

### 6.2 响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

### 6.3 分页格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [ ... ],
        "total": 128,
        "page": 1,
        "pageSize": 10
    }
}
```

### 6.4 异常处理

- 使用 `@ControllerAdvice` + `@ExceptionHandler` 全局捕获
- 业务异常使用自定义 `BizException`
- 参数校验异常返回 400 + 详细错误信息

---

## 七、数据库迁移

### 7.1 迁移工具

推荐使用 **Flyway** 或 **Liquibase** 管理数据库迁移。

### 7.2 迁移文件命名

```
V1__init_database.sql           -- 初始化所有表
V2__add_content_tables.sql      -- 内容模块表
V3__add_indexes.sql             -- 添加索引
```

### 7.3 初始化顺序

```sql
-- V1__init_database.sql
-- 1. 启用扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- 2. 按模块顺序创建表（见 database-schema.sql）

-- 3. 创建索引

-- 4. 不需要触发器，时间由应用层处理
```

---

## 八、测试策略

### 8.1 单元测试

- 每个 Service 方法编写单元测试
- 使用 H2 内存数据库隔离数据库依赖
- 测试覆盖率目标：核心业务逻辑 > 80%

### 8.2 集成测试

- 每个模块的 Controller 编写集成测试
- 使用 Testcontainers 启动 PostgreSQL 实例
- 测试完整请求链路

### 8.3 API 测试

- 使用 Postman/Apifox 编写 API 测试用例
- 覆盖所有 CRUD 操作
- 包含边界值测试

---

## 九、部署架构

### 9.1 推荐部署架构

```
                    ┌─────────────┐
                    │   Nginx     │
                    │  (反向代理)  │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌───▼───┐ ┌─────▼─────┐
       │  Spring Boot │ │ Redis │ │ PostgreSQL│
       │   (后端 API)  │ │(缓存) │ │  (数据库)  │
       └─────────────┘ └───────┘ └───────────┘
```

### 9.2 环境配置

| 环境 | 配置文件 | 说明 |
|------|---------|------|
| 开发 | `application-dev.yml` | 本地数据库、热重载 |
| 测试 | `application-test.yml` | 测试数据库、详细日志 |
| 生产 | `application-prod.yml` | 生产数据库、最小日志 |

---

## 十、文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| 数据库 Schema | `docs/backend-requirements/database-schema.md` | 完整数据库设计文档 |
| 建表 SQL | `docs/backend-requirements/database-schema.sql` | 可直接执行的建表脚本 |
| Content 模块 | `docs/backend-requirements/content/requirements.md` | Content 模块需求 |
| Portfolio 模块 | `docs/backend-requirements/portfolio/requirements.md` | Portfolio 模块需求 |
| Social 模块 | `docs/backend-requirements/social/requirements.md` | Social 模块需求 |
| Media 模块 | `docs/backend-requirements/media/requirements.md` | Media 模块需求 |
| Device 模块 | `docs/backend-requirements/device/requirements.md` | Device 模块需求 |
| System 模块 | `docs/backend-requirements/system/requirements.md` | System 模块需求 |
| Analytics 模块 | `docs/backend-requirements/analytics/requirements.md` | Analytics 模块需求 |

---

## 十一、开发检查清单

### Phase 1 检查清单
- [ ] 项目骨架搭建完成
- [ ] 数据库连接成功
- [ ] MyBatis-Plus 配置完成
- [ ] 统一响应体、异常处理完成
- [ ] System 模块 CRUD 完成
- [ ] 文件上传功能完成

### Phase 2 检查清单
- [ ] Content 模块 CRUD 完成
- [ ] 标签、分类关联正确
- [ ] 归档索引自动生成
- [ ] Diary 模块 CRUD 完成
- [ ] `PostPublished` 事件发布正确

### Phase 3 检查清单
- [ ] Portfolio 模块 CRUD 完成
- [ ] Device 模块 CRUD 完成
- [ ] 时间线事件排序正确

### Phase 4 检查清单
- [ ] Social 模块 CRUD 完成
- [ ] Media 模块 CRUD 完成
- [ ] `CommentPosted` 事件发布正确
- [ ] `PhotoUploaded` 事件发布正确

### Phase 5 检查清单
- [ ] Analytics 模块 CRUD 完成
- [ ] 事件订阅正确接收
- [ ] 每日统计定时任务正确

### Phase 6 检查清单
- [ ] 所有事件总线集成测试通过
- [ ] 热点数据缓存生效
- [ ] 核心接口性能达标
