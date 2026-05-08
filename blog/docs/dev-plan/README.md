# Mizuki 博客系统 — 后端业务逻辑开发总计划

> **文档类型**: 统领文件（Master Plan）
> **项目名称**: Mizuki Blog Backend Business Logic Implementation
> **技术栈**: JDK 25 / Spring Boot 3.5 / MyBatis-Flex / Sa-Token / PostgreSQL
> **架构模式**: DDD 严格四层（api → application → domain → infrastructure）
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 目录

1. [项目总览](#1-项目总览)
2. [模块体系与功能全景](#2-模块体系与功能全景)
3. [整体开发时间线与阶段划分](#3-整体开发时间线与阶段划分)
4. [模块间依赖关系与集成顺序](#4-模块间依赖关系与集成顺序)
5. [研发过程规范与质量保障](#5-研发过程规范与质量保障)
6. [各阶段交付物与验收标准](#6-各阶段交付物与验收标准)
7. [风险管理与应急预案](#7-风险管理与应急预案)
8. [附录：Service 任务编号总表](#8-附录service-任务编号总表)

---

## 1. 项目总览

### 1.1 项目背景

Mizuki 博客系统当前以 Astro 静态站点 + 本地数据文件（TypeScript 数组/Markdown 文件）的方式运行。前端已完成完整的页面设计与组件开发，后端目前仅有 Gradio 管理后台作为过渡方案。本次开发计划的目标是将所有内容管理能力从静态数据文件和 Gradio 迁移至基于 Spring Boot 的正规后端服务。

### 1.2 项目范围

本次计划**仅覆盖业务逻辑实现**，明确排除以下已在或将在其他专项中完成的工作：

| 已排除 | 说明 |
|--------|------|
| API 接口定义文档 | 已完成（`backend-requirements/*/api-*.md`） |
| 数据库表结构 | 已完成（`database-schema.sql`） |
| 认证与授权 | Sa-Token 基础设施（单独专项） |
| 对象存储/文件系统 | 基础设施层（单独专项） |
| 部署与 DevOps | CI/CD 流水线（单独专项） |

**本项目包含**：所有业务模块的 Domain/Application/Infrastructure 层代码实现，即 Entity、DomainService、AppService、Repository、RepositoryImpl、Mapper、PO、Request/VO、Assembler 等。

### 1.3 关键数字

| 指标 | 数值 |
|------|------|
| 业务模块数 | 6 |
| 数据表数 | 30 |
| Service 任务数 | 57 |
| Entity 实体数 | 28 |
| Repository 接口数 | 30 |
| 预计业务功能点 | 80+ |

---

## 2. 模块体系与功能全景

### 2.1 六大模块概览

```
┌─────────────────────────────────────────────────────────────┐
│                     Mizuki 后端 6 大模块                        │
├───────────┬──────────┬──────────┬──────────┬────────┬────────┤
│ MOD-01    │ MOD-02   │ MOD-03   │ MOD-04   │MOD-05  │MOD-06  │
│ Content   │Portfolio │  Media   │ Social   │Device  │System  │
│ 文章+日记  │项目+技能  │相册+番剧  │ 友链+评论 │ 设备   │ 系统   │
│           │ +时间线   │ +音乐    │          │        │ 通用    │
├───────────┼──────────┼──────────┼──────────┼────────┼────────┤
│ 14+8      │ 8+6+8    │ 8+5+6    │ 6+6      │ 3+5    │ 3+3+3+ │
│ 功能点    │ 功能点    │ 功能点    │ 功能点    │ 功能点  │ 3+3+2+ │
│           │          │          │          │        │ 5 功能点│
├───────────┼──────────┼──────────┼──────────┼────────┼────────┤
│ 15 个     │ 10 个    │ 9 个     │ 6 个     │ 4 个   │ 13 个  │
│ Service   │ Service  │ Service  │ Service   │Service │Service │
└───────────┴──────────┴──────────┴──────────┴────────┴────────┘
```

### 2.2 模块文档索引

| 模块 | 文档路径 | 核心内容 |
|------|---------|---------|
| MOD-01 Content | [module-01-content.md](./module-01-content.md) | 文章/标签/分类/归档/日记全文CRUD + 搜索 + 推荐 + 加密 |
| MOD-02 Portfolio | [module-02-portfolio.md](./module-02-portfolio.md) | 项目/技能/时间线CRUD + 关联表管理 + 聚合统计 |
| MOD-03 Media | [module-03-media.md](./module-03-media.md) | 相册/番剧/音乐CRUD + 图片管理 + 布局控制 |
| MOD-04 Social | [module-04-social.md](./module-04-social.md) | 友链/标签/评论CRUD + 审核流 + 防刷 + 嵌套回复 |
| MOD-05 Device | [module-05-device.md](./module-05-device.md) | 设备/分类CRUD + JSONB规格 + 分组展示 |
| MOD-06 System | [module-06-system.md](./module-06-system.md) | 配置/上传/导航/横幅/主题/统计/公告/搜索日志 |

---

## 3. 整体开发时间线与阶段划分

### 3.1 四阶段里程碑

```
Phase 1 ──── Phase 2 ──── Phase 3 ──── Phase 4
(基础设施)    (核心业务)    (扩展业务)    (系统完善)
  MOD-06       MOD-01       MOD-03       MOD-06
  基础部分      MOD-02       MOD-04       完善部分
               MOD-06       MOD-05
               Nav/Banner
```

### 3.2 Phase 1：基础设施奠基（P0 — 必须最先完成）

| 任务 | 所属模块 | 说明 |
|------|---------|------|
| FileUploadService (S47) | MOD-06 | **核心通用服务**，被所有模块依赖 |
| SiteConfig CRUD (S45) | MOD-06 | 站点配置存储与读取 |
| CustomPages CRUD (S46) | MOD-06 | About 等页面内容管理 |
| FileRecordService (S48) | MOD-06 | 上传记录追踪 |

**验收标准**：
- [ ] FileUploadService 可接收各 folder 的文件上传并返回 URL
- [ ] 文件格式/大小校验通过，白名单 + MIME 魔术数字双重校验
- [ ] SiteConfig 的 get/update/batchGet 接口正常
- [ ] CustomPages 的 get/upsert/delete 接口正常

### 3.3 Phase 2：核心业务（P0/P1 — 博客基本内容管理）

| 子阶段 | 任务 | 所属模块 | 优先级 |
|--------|------|---------|--------|
| 2a | Posts CRUD + Slug + 字数统计 + 标签关联 | MOD-01 | P0 |
| 2b | 分类 CRUD + 标签 CRUD + 归档维护 | MOD-01 | P0 |
| 2c | 全文搜索 + 相关推荐 + 加密验证 | MOD-01 | P1 |
| 2d | Projects CRUD + 技术栈/标签关联 | MOD-02 | P0 |
| 2e | Diary CRUD + 标签筛选 + 图片管理 | MOD-01 | P1 |
| 2f | NavLinks + Banners + ThemeSettings CRUD | MOD-06 | P1 |

**验收标准**：
- [ ] 文章完整 CRUD + 列表分页 + 排序 + 筛选
- [ ] 文章 slug 自动生成 + permalink 占位符解析
- [ ] 文章创建/更新/删除时事务性维护标签关联和归档
- [ ] 标签/分类独立 CRUD + 树状结构
- [ ] 项目 CRUD + 技术栈/标签筛选
- [ ] 导航链接树状查询正常
- [ ] 加密文章密码验证正常

### 3.4 Phase 3：扩展业务（P1/P2 — 博客完整内容生态）

| 子阶段 | 任务 | 所属模块 | 优先级 |
|--------|------|---------|--------|
| 3a | Skills CRUD + Timeline CRUD | MOD-02 | P1 |
| 3b | Albums CRUD + 图片管理 | MOD-03 | P1 |
| 3c | Friends CRUD + Comments CRUD | MOD-04 | P1 |
| 3d | Anime CRUD + Music CRUD | MOD-03 | P2 |
| 3e | Devices CRUD + 分类管理 | MOD-05 | P2 |

**验收标准**：
- [ ] 时间线事件含附属数据（技能/成就/链接）完整 CRUD
- [ ] 相册上传图片 → 设置封面 → 删除图片 → 封面自动切换 完整流程
- [ ] 评论审核流（游客待审 → 管理员通过）
- [ ] 友链头像外链/本地上传两种模式正常
- [ ] 番剧评分/进度校验正常
- [ ] 设备 JSONB 规格参数存取正常

### 3.5 Phase 4：系统完善（P2/P3 — 运营支撑）

| 任务 | 所属模块 | 优先级 |
|------|---------|--------|
| Analytics（page_views + daily_stats + search_logs） | MOD-06 | P2 |
| Announcements CRUD | MOD-06 | P2 |
| SiteStatService 跨模块聚合 | MOD-06 | P3 |
| 批量操作（文章/项目批量删除等） | MOD-01/02 | P2 |

**验收标准**：
- [ ] 页面访问异步记录不阻塞主请求
- [ ] 每日统计定时任务正常执行，数据准确
- [ ] 公告按时段展示
- [ ] 搜索日志正确记录
- [ ] 站点统计面板数据准确

---

## 4. 模块间依赖关系与集成顺序

### 4.1 依赖拓扑图

```
                    ┌─────────────────────┐
                    │      MOD-06          │
                    │   System (系统通用)    │
                    │ ┌───────────────────┐ │
                    │ │ FileUploadService │◄├──────────┬──────────┬──────────┬──────────┐
                    │ │ SystemConfigSvc   │ │          │          │          │          │
                    │ │ SiteStatService   │ │       MOD-01     MOD-02     MOD-03     MOD-04
                    │ └───────────────────┘ │      Content   Portfolio    Media     Social
                    └─────────┬─────────────┘          │                    │
                              │                        │                    │
                              │   提供通用服务给         │                    │
                              │   所有业务模块           │                    │
                              ▼                        ▼                    ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │              各业务模块独立开发，通过接口协作                │
                    │  MOD-01 ←─(评论数查询)─→ MOD-04                           │
                    │  MOD-02 ←─(项目关联)──→ MOD-02 内部 Skills↔Projects       │
                    │  MOD-06 ←─(统计聚合)──→ MOD-01/02/03/04/05               │
                    └─────────────────────────────────────────────────────────┘
```

### 4.2 集成顺序矩阵

| 顺序 | 模块 | 前置条件 | 后续依赖方 |
|------|------|---------|-----------|
| 1 | MOD-06 Phase 1（FileUpload + SiteConfig） | 无 | **所有模块** |
| 2 | MOD-01 Phase 2a（Posts 基础 CRUD） | MOD-06 P1 | MOD-04(评论验证) |
| 2 | MOD-02 Phase 2d（Projects CRUD） | MOD-06 P1 | MOD-06(统计) |
| 3 | MOD-01 Phase 2b/c（标签/分类/归档/搜索） | MOD-01 P2a | MOD-06(统计) |
| 3 | MOD-04 Phase 3c（Friends + Comments） | MOD-06 P1 + MOD-01 P2a | MOD-01(评论数) |
| 4 | MOD-03 Phase 3b（Albums） | MOD-06 P1 | MOD-06(统计) |
| 4 | MOD-02 Phase 3a（Skills + Timeline） | MOD-06 P1 | MOD-06(统计) |
| 5 | MOD-05 Phase 3e（Devices） | MOD-06 P1 | MOD-06(统计) |
| 6 | MOD-03 Phase 3d（Anime + Music） | MOD-06 P1 | — |
| 7 | MOD-06 Phase 4（Analytics + 统计聚合） | MOD-01/02/03/04/05 | — |

### 4.3 关键集成接口契约

| 服务提供方 | 服务接口 | 消费方 |
|-----------|---------|--------|
| MOD-06 FileUploadService | `upload(file, folder) → UploadedFileVO` | MOD-01,02,03,04,05 |
| MOD-06 SystemConfigService | `getConfig(key) → SiteConfigVO` | MOD-01,02,03,04,05 |
| MOD-04 CommentCountService | `getCount(targetType, targetId) → int` | MOD-01 |
| MOD-01 PostService | `exists(postId) → boolean` | MOD-04（评论目标验证） |
| MOD-06 SiteStatService | 聚合查询各模块 Repository | 前端统计面板 |

---

## 5. 研发过程规范与质量保障

### 5.1 开发流程规范

```
需求对齐 → 技术方案 → 编码实现 → 自测验证 → Code Review → 集成测试 → 验收
```

| 环节 | 要求 |
|------|------|
| 需求对齐 | 对照 `backend-requirements/*/module-*.md` 逐功能确认 |
| 技术方案 | 复杂功能（推荐算法/加密/全文搜索）写出 3-5 行设计思路 |
| 编码实现 | 严格遵循四层架构 + 编码约定（禁止 JOIN、禁止 @Transactional、禁止 Builder 链式构造等） |
| 自测验证 | 运行已有测试 + 补充新测试用例 |
| Code Review | 对照本计划中的业务规则逐条检查 |
| 集成测试 | 模块间接口联调，确认数据流转正确 |

### 5.2 编码规范强制项

| 强制规则 | 依据 |
|---------|------|
| 禁止 Mapper 添加自定义方法 | 架构规范 |
| 所有 DB 操作在 RepositoryImpl 中完成 | 架构规范 |
| 禁止 @Transactional → 使用 TransactionTemplate | 编码约定 |
| 枚举字段必须定义枚举类，禁止魔法值 | 编码约定 |
| 参数校验使用 @Check + SmartValidator | 编码约定 |
| 对象转换使用 MapStruct | 编码约定 |
| 审计字段由框架自动填充 | 编码约定 |
| 禁止手动构造 BaseResponse → 使用 Asserts 抛异常 | 编码约定 |
| 方法体 ≤ 120 行，参数 ≤ 4 个 | 编码约定 |

### 5.3 质量门禁

| 门禁 | 通过标准 |
|------|---------|
| 编译 | `mvn compile` 零错误 |
| 测试 | 新增业务功能对应的单元测试全部通过 |
| Lint | 代码风格检查通过 |
| CR | 至少一人 Review 通过 |
| 集成 | 模块间接口调用正常 |

### 5.4 文档交付要求

每个模块完成后产出：
1. 模块 README（1 页概述 + 关键设计决策）
2. API 接口对照表（与 api-*.md 需求文档的映射）

---

## 6. 各阶段交付物与验收标准

### 6.1 Phase 1 交付物清单

| 交付物 | 类型 | 验收方式 |
|--------|------|---------|
| FileUploadService.java | 代码 | 上传 jpg/png/webp → 返回正确 URL + 宽高 |
| FileRecordService.java | 代码 | uploaded_files 表记录正确 |
| SystemConfigService.java | 代码 | get/update/batchGet 三接口可用 |
| CustomPageService.java | 代码 | get by page_key + upsert + delete 可用 |
| 对应 Repository/Impl/PO/VO | 代码 | 符合四层架构规范 |
| 单元测试 | 测试 | 文件校验逻辑覆盖通过 |

### 6.2 Phase 2 交付物清单

| 交付物 | 类型 | 验收方式 |
|--------|------|---------|
| PostDomainService + PostAppService | 代码 | 文章 CRUD + slug 生成 + 标签关联 |
| TagDomainService + TagAppService | 代码 | 标签 CRUD + slug 生成 |
| CategoryDomainService + CategoryAppService | 代码 | 分类 CRUD + 树结构组装 |
| ArchiveDomainService + ArchiveAppService | 代码 | 归档 upsert + 查询 |
| PostRecommendService | 代码 | 推荐算法返回结果合理 |
| ProjectDomainService + ProjectAppService | 代码 | 项目 CRUD + 技术栈/标签关联 |
| NavLinkService + BannerService + ThemeSettingService | 代码 | 导航树、横幅 CRUD、主题 CRUD |
| DiaryDomainService + DiaryAppService | 代码 | 日记 CRUD + JSONB 标签筛选 |

### 6.3 Phase 3 交付物清单

| 交付物 | 类型 | 验收方式 |
|--------|------|---------|
| SkillService + TimelineService | 代码 | 技能/时间线 CRUD + 附属数据 |
| AlbumService + AlbumPhotoService | 代码 | 相册/图片 CRUD + 封面切换 |
| FriendService + CommentService | 代码 | 友链/评论 CRUD + 审核流 |
| AnimeService + MusicService | 代码 | 番剧/音乐 CRUD |
| DeviceService + DeviceCategoryService | 代码 | 设备/分类 CRUD |

### 6.4 Phase 4 交付物清单

| 交付物 | 类型 | 验收方式 |
|--------|------|---------|
| AnalyticsService + DailyStatsService | 代码 | 访问记录异步写 + 每日统计准确 |
| SearchLogService + AnnouncementService | 代码 | 搜索日志 + 公告时段展示 |
| SiteStatService | 代码 | 跨模块聚合统计面板数据 |
| 批量操作接口 | 代码 | 批量删除/修改功能正常 |

---

## 7. 风险管理与应急预案

### 7.1 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| PostgreSQL JSONB 查询性能不足 | 日记/相册/技能等模块查询慢 | 中 | 已设计 GIN 索引；必要时对高频 JSONB 查询增加物化视图 |
| 全文搜索 GIN 索引在高数据量下变慢 | 搜索功能响应慢 | 低 | 初始数据量小；预留 Elasticsearch 迁移接口 |
| 加密文章 Token 机制被绕过 | 安全漏洞 | 低 | JWT 签名验证 + 短有效期(30min) + Token 与文章 ID 绑定 |
| 乐观锁 version 冲突频繁 | 并发编辑失败率高 | 低 | 博客系统为单用户管理，并发场景极少 |
| MyBatis-Flex 子查询不支持 | 标签筛选实现复杂 | 低 | 可用 EXISTS 子查询替代 |

### 7.2 进度风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| Service 任务序号 57 个，体量大 | 开发周期长 | 高 | 按优先级分 4 阶段交付；低优先级（P2/P3）可延后 |
| 模块间依赖导致阻塞 | MOD-06 未完成时其他模块无法启动 | 中 | MOD-06 P1 只实现最小可用接口（FileUpload/SiteConfig/CustomPages 共 ~4 个 Service） |
| 前端联调发现数据结构不匹配 | 返工 | 中 | 开发前逐模块确认 VO 字段与前端 TS 接口一致性 |

### 7.3 应急方案

| 场景 | 应急措施 |
|------|---------|
| MOD-06 核心服务延迟 | 各业务模块先用 Mock 实现做并行开发，待 MOD-06 完成后切换 |
| 某模块超出预估工期 | 拆分更小粒度功能，优先交付 P0/P1，P2 进入 backlog |
| 数据库性能问题 | 启用查询缓存（Caffeine），对热点数据（归档/标签列表）加缓存层 |
| 业务规则遗漏 | 对照前端代码中实际消费的数据格式做兜底检查 |

---

## 8. 附录：Service 任务编号总表

### 8.1 全部 57 个 Service 任务索引

| 编号 | 类名 | 模块 | 层级 | 优先级 |
|------|------|------|------|--------|
| S01 | PostDomainService | MOD-01 Content | Domain | P0 |
| S02 | PostAppService | MOD-01 Content | Application | P0 |
| S03 | TagDomainService | MOD-01 Content | Domain | P0 |
| S04 | TagAppService | MOD-01 Content | Application | P0 |
| S05 | CategoryDomainService | MOD-01 Content | Domain | P0 |
| S06 | CategoryAppService | MOD-01 Content | Application | P0 |
| S07 | ArchiveDomainService | MOD-01 Content | Domain | P0 |
| S08 | ArchiveAppService | MOD-01 Content | Application | P0 |
| S09 | PostRecommendService | MOD-01 Content | Domain | P1 |
| S10 | DiaryDomainService | MOD-01 Content | Domain | P1 |
| S11 | DiaryAppService | MOD-01 Content | Application | P1 |
| S12 | DiaryImageService | MOD-01 Content | Domain | P1 |
| S13 | SlugService | MOD-01 Content | Domain | P0 |
| S14 | WordCountService | MOD-01 Content | Domain | P0 |
| S15 | SearchService | MOD-01 Content | Domain | P1 |
| S16 | ProjectDomainService | MOD-02 Portfolio | Domain | P0 |
| S17 | ProjectAppService | MOD-02 Portfolio | Application | P0 |
| S18 | TechStackService | MOD-02 Portfolio | Domain | P0 |
| S19 | SkillDomainService | MOD-02 Portfolio | Domain | P1 |
| S20 | SkillAppService | MOD-02 Portfolio | Application | P1 |
| S21 | SkillCategoryService | MOD-02 Portfolio | Domain | P1 |
| S22 | TimelineDomainService | MOD-02 Portfolio | Domain | P1 |
| S23 | TimelineAppService | MOD-02 Portfolio | Application | P1 |
| S24 | TimelineEventTypeService | MOD-02 Portfolio | Domain | P1 |
| S25 | PortfolioStatService | MOD-02 Portfolio | Domain | P2 |
| S26 | AlbumDomainService | MOD-03 Media | Domain | P1 |
| S27 | AlbumAppService | MOD-03 Media | Application | P1 |
| S28 | AlbumPhotoService | MOD-03 Media | Domain | P1 |
| S29 | AnimeDomainService | MOD-03 Media | Domain | P2 |
| S30 | AnimeAppService | MOD-03 Media | Application | P2 |
| S31 | MusicPlaylistDomainService | MOD-03 Media | Domain | P2 |
| S32 | MusicPlaylistAppService | MOD-03 Media | Application | P2 |
| S33 | MusicTrackDomainService | MOD-03 Media | Domain | P2 |
| S34 | MusicTrackAppService | MOD-03 Media | Application | P2 |
| S35 | FriendDomainService | MOD-04 Social | Domain | P1 |
| S36 | FriendAppService | MOD-04 Social | Application | P1 |
| S37 | FriendTagService | MOD-04 Social | Domain | P1 |
| S38 | CommentDomainService | MOD-04 Social | Domain | P1 |
| S39 | CommentAppService | MOD-04 Social | Application | P1 |
| S40 | CommentCountService | MOD-04 Social | Domain | P1 |
| S41 | DeviceCategoryDomainService | MOD-05 Device | Domain | P2 |
| S42 | DeviceCategoryAppService | MOD-05 Device | Application | P2 |
| S43 | DeviceDomainService | MOD-05 Device | Domain | P2 |
| S44 | DeviceAppService | MOD-05 Device | Application | P2 |
| S45 | SystemConfigService | MOD-06 System | Domain | P0 |
| S46 | CustomPageService | MOD-06 System | Domain | P0 |
| S47 | **FileUploadService** | MOD-06 System | Domain | **P0** |
| S48 | FileRecordService | MOD-06 System | Domain | P0 |
| S49 | NavLinkDomainService | MOD-06 System | Domain | P1 |
| S50 | NavLinkAppService | MOD-06 System | Application | P1 |
| S51 | BannerService | MOD-06 System | Domain | P1 |
| S52 | ThemeSettingService | MOD-06 System | Domain | P1 |
| S53 | AnalyticsService | MOD-06 System | Domain | P2 |
| S54 | DailyStatsService | MOD-06 System | Domain | P2 |
| S55 | SearchLogService | MOD-06 System | Domain | P2 |
| S56 | AnnouncementService | MOD-06 System | Domain | P2 |
| S57 | SiteStatService | MOD-06 System | Domain | P3 |

### 8.2 优先级分布统计

| 优先级 | 数量 | 说明 |
|--------|------|------|
| P0 | 18 | 核心基础设施 + 基础 CRUD，Phase 1-2 必须完成 |
| P1 | 22 | 扩展业务功能，Phase 2-3 完成 |
| P2 | 16 | 低频功能/增强功能，Phase 3-4 完成 |
| P3 | 1 | 站点统计聚合，Phase 4 最后完成 |

---

## 文档更新记录

| 日期 | 版本 | 变更内容 |
|------|------|---------|
| 2026-05-08 | v1.0 | 初始版本，完整 6 模块 + 统领文件 |
