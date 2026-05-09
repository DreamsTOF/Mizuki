# Mizuki 后端开发计划 — System 模块（系统通用）

> **模块编号**: MOD-06
> **包含子模块**: SiteConfig（站点配置）、CustomPages（自定义页面）、FileUpload（文件上传）、NavLinks（导航链接）、Banners（横幅）、ThemeSettings（主题设置）、Analytics（统计/搜索/公告）
> **涉及数据表**: site_configs, custom_pages, uploaded_files, nav_links, banners, theme_settings, page_views, daily_stats, search_logs, announcements
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

System 模块是 Mizuki 博客的**基础设施层**，承载所有业务模块共享的通用能力。它是整个后端系统的"底座"——站点配置、文件上传、导航、横幅、主题、统计分析全部在此模块中实现。该模块**被所有其他模块依赖**，本身不依赖任何业务模块。

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 站点配置 | 集中式 key-value 配置存储（JSONB 支持嵌套）+ CRUD |
| 文件上传 | 通用文件上传服务，供各业务模块使用 + 上传记录追踪 |
| 自定义页面 | Markdown 内容页面管理（About 等独立页面） |
| 导航管理 | 多级导航链接 + 多位置（navbar/footer/drawer）+ 排序 |
| 横幅管理 | 桌面/移动端横幅图片 + 轮播配置 + 全屏壁纸 |
| 主题设置 | 主题色/字体/布局等用户自定义设置存储 |
| 统计分析 | 页面访问记录 + 每日统计汇总 + 搜索日志 |
| 公告管理 | 站点公告 CRUD + 定时展示（start_time/end_time）+ 可关闭 |

### 1.3 前后端边界

- **前端负责**：配置消费、导航渲染、横幅展示、主题应用、统计面板 UI
- **后端负责**：配置存取、文件存储、访问日志写入、搜索日志记录、统计聚合计算

---

## 2. 核心业务逻辑实现任务

### 2.1 SiteConfig 子模块（3 个业务功能）

#### 2.1.1 按 key 获取配置
- config_value 以 JSONB 原样返回，前端自行解析嵌套结构
- 常用 key：`site_meta`、`appearance`、`feature_pages`、`navbar_config`、`banner_config`、`post_list_config`、`third_party`、`effects`、`other`

#### 2.1.2 批量获取配置
- 传入 key 数组，批量返回（减少网络往返）

#### 2.1.3 更新配置
- upsert 语义：config_key 存在则更新，不存在则创建
- version 乐观锁防并发

---

### 2.2 CustomPages 子模块（3 个业务功能）

#### 2.2.1 按 page_key 获取页面内容
- page_key 唯一标识：`about`、`disclaimer` 等
- 仅返回 `has_enabled=TRUE` 的页面

#### 2.2.2 页面创建/更新
- content 存储 Markdown 原文
- 可选：title/description/cover_image/has_comment_enabled

#### 2.2.3 页面删除
- 软删除

---

### 2.3 FileUpload 子模块（核心通用服务，3 个业务功能）

#### 2.3.1 文件上传（单文件/批量）
- **核心逻辑**：
  - 接收文件 + target_folder（如 posts/diary/albums/devices/avatars/assets）
  - 校验：格式白名单（按 folder 不同）、大小限制（按 folder 不同）
  - 生成唯一存储文件名：`{yyyyMMdd}_{HHmmss}_{UUID8}.{ext}`
  - 存储到文件系统或对象存储
  - 图片类型解析宽高 + 生成缩略图（可选）
  - 写 uploaded_files 记录表
- **输出**：UploadedFileVO（url/width/height/mime_type/thumbnail_url）

#### 2.3.2 文件删除
- 基于 id 或 url 物理删除文件 + 软删除数据库记录

#### 2.3.3 文件列表查询
- 按 folder 筛选 + 分页

---

### 2.4 NavLinks 子模块（3 个业务功能）

#### 2.4.1 导航链接列表（按位置分组）
- 按 position（navbar/footer/drawer）分组 + 组内 sort_order ASC
- parent_id 自引用 → 应用层组装树（最多 2 层）

#### 2.4.2 导航链接创建/更新
- 必填：name + url + position
- 可选：icon + has_external + has_new_window + parent_id + sort_order

#### 2.4.3 导航链接删除
- 软删除；若有子链接 → 拒绝或级联删除

---

### 2.5 Banners 子模块（3 个业务功能）
- 横幅列表：按 position + device_type 筛选 + sort_order ASC
- 创建/更新：image_url + position 必填，可选 title/device_type/has_carousel/has_enabled
- 删除：软删除 + 清理图片文件

---

### 2.6 ThemeSettings 子模块（2 个业务功能）
- 按 setting_key 获取（JSONB 返回）
- upsert 更新
- `has_user_customizable` 控制公开接口权限

---

### 2.7 Analytics 子模块（5 个业务功能）

#### 2.7.1 页面访问记录（异步写入）
- VirtualTaskManager 异步写 page_views 表，不阻塞请求
- 字段：page_path/page_type/target_id/ip_address/user_agent/referer

#### 2.7.2 每日统计聚合
- 定时任务（每日凌晨）：聚合前一天的 PV/UV/文章阅读数/评论数 → daily_stats

#### 2.7.3 站点统计查询
- 聚合所有业务模块的数据：文章/评论/项目/技能/友链/相册/设备数量
- 运行天数 = now() - site_start_date
- 最后更新 = 最新文章发布日期

#### 2.7.4 搜索日志
- 异步记录 keyword + result_count + ip_address
- 热词查询：按频次降序 + 时间窗口可选

#### 2.7.5 公告管理（Announcements CRUD）
- 创建/更新：title/content/link_text/link_url/has_closable/has_enabled/start_time/end_time
- 列表：仅返回 `(start_time IS NULL OR start_time <= now()) AND (end_time IS NULL OR end_time >= now())` 的公告

---

## 3. 业务规则实现详情

### 3.1 文件上传安全规则
| Folder | 格式白名单 | 大小上限 |
|--------|----------|---------|
| posts/albums | jpg,jpeg,png,webp,gif | 5MB |
| diary | jpg,jpeg,png,webp,gif | 3MB |
| avatars | jpg,jpeg,png,webp | 2MB |
| assets | jpg,jpeg,png,webp,svg,ico | 10MB |

MIME 类型 + 文件魔术数字双重校验，不仅校扩展名。

### 3.2 导航多级菜单
- parent_id=NULL → 顶级链接
- 一次性查出某 position 下所有链接 → HashMap 组装父子关系
- 最大嵌套 2 层

### 3.3 公告定时展示
- `start_time` NULL → 立即开始 / `end_time` NULL → 永久有效
- 查询：`(start_time IS NULL OR start_time <= now()) AND (end_time IS NULL OR end_time >= now())`

### 3.4 每日统计去重
- UV：同一天同 IP 同 page_path 计 1
- PV：每次访问计 1
- 聚合 SQL 按 `visited_at::date` 分组

---

## 4. Service 层开发任务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S45 | SystemConfigService | domain/service/SystemConfigService.java | 配置 CRUD + 版本管理 |
| S46 | CustomPageService | domain/service/CustomPageService.java | 自定义页面 CRUD |
| S47 | **FileUploadService** | domain/service/FileUploadService.java | **核心通用服务：文件上传/删除/校验 → 供所有模块调用** |
| S48 | FileRecordService | domain/service/FileRecordService.java | uploaded_files 表管理 |
| S49 | NavLinkDomainService | domain/service/NavLinkDomainService.java | 导航树组装、级联校验 |
| S50 | NavLinkAppService | application/service/NavLinkAppService.java | 导航 CRUD 编排 |
| S51 | BannerService | domain/service/BannerService.java | 横幅 CRUD + 图片清理 |
| S52 | ThemeSettingService | domain/service/ThemeSettingService.java | 主题设置 CRUD |
| S53 | AnalyticsService | domain/service/AnalyticsService.java | 访问记录写入 + 统计聚合 |
| S54 | DailyStatsService | domain/service/DailyStatsService.java | 每日统计计算 + 查询 |
| S55 | SearchLogService | domain/service/SearchLogService.java | 搜索日志记录 + 热词查询 |
| S56 | AnnouncementService | domain/service/AnnouncementService.java | 公告 CRUD + 时间范围过滤 |
| S57 | SiteStatService | domain/service/SiteStatService.java | 跨模块站点统计聚合 |

---

## 5. 对象模型设计

### 5.1 Entity 领域实体

| 实体 | 关键方法 |
|------|---------|
| SiteConfigEntity | `getNestedValue(path)`, `setNestedValue(path, value)` |
| CustomPageEntity | `isEnabled()`, `hasComments()` |
| UploadedFileEntity | `isImage()`, `getThumbnailUrl()` |
| NavLinkEntity | `isTopLevel()`, `hasChildren()`, `isExternal()` |
| BannerEntity | `isCarousel()`, `isActiveNow()` |
| ThemeSettingEntity | `isUserCustomizable()` |
| PageViewEntity | (纯数据) |
| DailyStatEntity | (纯数据) |
| SearchLogEntity | (纯数据) |
| AnnouncementEntity | `isActiveNow()`, `canBeClosed()` |

### 5.2 PO 持久化对象

| PO | 对应表 |
|----|--------|
| SiteConfigPO.java | site_configs |
| CustomPagePO.java | custom_pages |
| UploadedFilePO.java | uploaded_files |
| NavLinkPO.java | nav_links |
| BannerPO.java | banners |
| ThemeSettingPO.java | theme_settings |
| PageViewPO.java | page_views |
| DailyStatPO.java | daily_stats |
| SearchLogPO.java | search_logs |
| AnnouncementPO.java | announcements |

### 5.3 Repository

| Repository | 包路径 |
|------------|--------|
| SiteConfigRepository | domain/repository/SiteConfigRepository.java |
| CustomPageRepository | domain/repository/CustomPageRepository.java |
| UploadedFileRepository | domain/repository/UploadedFileRepository.java |
| NavLinkRepository | domain/repository/NavLinkRepository.java |
| BannerRepository | domain/repository/BannerRepository.java |
| ThemeSettingRepository | domain/repository/ThemeSettingRepository.java |
| PageViewRepository | domain/repository/PageViewRepository.java |
| DailyStatRepository | domain/repository/DailyStatRepository.java |
| SearchLogRepository | domain/repository/SearchLogRepository.java |
| AnnouncementRepository | domain/repository/AnnouncementRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 配置 VO | SiteConfigVO | config_key + config_value(JSONB) |
| 页面 VO | CustomPageVO | |
| 文件 VO | UploadedFileVO | url/width/height/mime_type/thumbnail_url |
| 导航 VO | NavLinkVO, NavLinkTreeVO | 树状结构 |
| 横幅 VO | BannerVO | |
| 主题 VO | ThemeSettingVO | |
| 统计 VO | DailyStatVO, SiteStatVO, SearchHotVO | |
| 公告 VO | AnnouncementVO | |

---

## 6. 集成点

### 6.1 被所有业务模块依赖的通用服务
- **FileUploadService**：MOD-01(文章/日记封面)、MOD-02(项目封面)、MOD-03(相册图片)、MOD-04(友链头像)、MOD-05(设备图片)
- **SystemConfigService**：MOD-01(permalink_format)、MOD-02~05(featurePages 开关)
- **CommentCountService**：MOD-01 文章评论数查询

### 6.2 消费各业务模块的数据
- **SiteStatService**：聚合 Content(文章/分类/标签/字数) + Social(评论数) + Portfolio(项目/技能/事件) + Media(相册/番剧) + Device(设备) + Friends(友链)

### 6.3 外部服务集成
- 页面访问记录由前端通过专用埋点接口上报
- 搜索日志由搜索功能写入
- 公告由前端定期拉取

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | 10 张系统表 | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| VirtualTaskManager | 异步记录访问/搜索日志 | 框架 |
| 文件存储（本地/OSS） | 文件物理存储 | 基础设施 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| **所有业务模块** | FileUploadService | 图片上传通用服务 |
| **所有业务模块** | SystemConfigService | 站点配置读取 |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | FileUploadService + SiteConfig CRUD + CustomPages CRUD | P0（被所有模块依赖，必须最先完成） |
| Phase 2 | NavLinks CRUD + Banners CRUD + ThemeSettings CRUD | P1 |
| Phase 3 | Analytics（page_views + daily_stats + search_logs + announcements） | P2 |
| Phase 4 | SiteStatService 跨模块聚合 | P3 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 文件上传安全 | MIME + 魔术数字双重校验，白名单过滤 |
| 访问日志性能 | 异步写入，不阻塞主请求（≤5ms 额外开销） |
| 每日统计准确性 | 定时任务可重复执行不产生重复数据（幂等） |
| 配置热更新 | 支持静态缓存 + 版本号触发刷新 |
| 导航树查询 | 单次查询，应用层组装，≤50ms |
