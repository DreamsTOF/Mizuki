# Mizuki 后端开发计划 — Portfolio 模块（项目 + 技能 + 时间线）

> **模块编号**: MOD-02
> **包含子模块**: Projects（项目）、Skills（技能）、Timeline（时间线）
> **涉及数据表**: projects, project_tech_stacks, project_tags, skills, timeline_events, timeline_event_skills, timeline_event_achievements, timeline_event_links
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

Portfolio 模块是 Mizuki 博客的**个人作品集展示引擎**，由三个紧密关联的子模块构成：Projects（项目展示）、Skills（技能矩阵）、Timeline（人生时间线）。三者共同打造完整的个人品牌形象：

- **Projects** 回答"我做过什么"——以卡片网格展示项目作品
- **Skills** 回答"我会什么"——以技能矩阵展示技术栈和能力等级
- **Timeline** 回答"我经历过什么"——以时间轴展示教育/工作/项目/成就

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 项目展示 | 完整 CRUD + 类别筛选 + 置顶精选 + 技术栈/标签关联 |
| 技能矩阵 | 完整 CRUD + 分类/等级多维筛选 + 项目关联 + 证书管理 |
| 时间线 | 完整 CRUD + 类型筛选 + 技能/成就/链接附属数据管理 + featured 标记 |
| 聚合统计 | 项目总数/状态分布/分类分布、技能分类统计、时间线类型统计 |
| 数据一致性 | 技能↔项目、技能↔时间线事件的名称引用一致性维护 |

### 1.3 前后端边界

- **前端负责**：卡片渲染、FilterTabs 筛选交互、时间轴动画、响应式网格布局、图标渲染（Iconify）
- **后端负责**：数据 CRUD、筛选排序、关联表管理、聚合统计

---

## 2. 核心业务逻辑实现任务

### 2.1 Projects 子模块（8 个业务功能）

#### 2.1.1 项目列表查询（带筛选）
- **输入**：分页参数（page/size）、category 筛选、status 筛选、featured 筛选、techStack 筛选（精确匹配技术名）
- **核心逻辑**：
  - 默认排序：`has_featured DESC → sort_order ASC → created_at DESC`
  - 分类筛选：`category = ?`（VARCHAR 枚举：web/mobile/desktop/other）
  - 技术栈筛选：通过 project_tech_stacks 子查询
  - 标签筛选：通过 project_tags 子查询
- **输出**：分页后的 ProjectVO 列表 + 技术栈数组 + 标签数组

#### 2.1.2 项目详情查询

#### 2.1.3 项目创建
- **核心逻辑**：
  - `id`：UUID 主键（业务层生成），同时保留自定义字符串 `id` 作为唯一标识字段
  - `category`/`status`：枚举校验
  - 技术栈关联：先写 projects → 再写 project_tech_stacks（全量写入，每个 tech_name 一条记录）
  - 标签关联：先写 projects → 再写 project_tags（全量写入）
  - `start_date` 必填 + `end_date` 可选（须 ≥ start_date）
  - **事务范围**：projects + project_tech_stacks + project_tags

#### 2.1.4 项目更新
- **核心逻辑**：部分字段更新 + 技术栈/标签全量替换（先删旧关联 → 再写新关联）
- 状态变为 `completed` 时，若 `end_date` 为空则自动填充当前日期
- **事务范围**：projects + project_tech_stacks + project_tags

#### 2.1.5 项目删除
- 软删除 + 级联删除关联表（project_tech_stacks + project_tags）
- 清理封面图片物理文件（检查是否被其他项目引用）

#### 2.1.6 技术栈列表查询
- 从 project_tech_stacks 聚合去重 + 按字母排序
- 每个技术栈附带使用该技术的项目数量

#### 2.1.7 项目分类列表
- 返回枚举值 `["web","mobile","desktop","other"]` + 每类项目数量

#### 2.1.8 项目统计信息
- total / byStatus（completed/inProgress/planned）/ byCategory / featuredCount

---

### 2.2 Skills 子模块（6 个业务功能）

#### 2.2.1 技能列表查询（带筛选）
- **输入**：category 筛选（frontend/backend/database/tools/other）、level 筛选（beginner/intermediate/advanced/expert）
- **核心逻辑**：按分类 + 等级排序，支持组合筛选
- `projects` JSONB 字段存储关联项目 ID 列表，`certifications` JSONB 字段存储证书名称列表
- **输出**：分页后的 SkillVO 列表

#### 2.2.2 技能详情查询

#### 2.2.3 技能创建
- 必填字段：name/description/icon/category/level/experience（years + months 拆分为 experience_years + experience_months）
- 枚举校验：category（5 个值）、level（4 个值）
- `color`：可选 HEX 颜色
- `projects`：JSONB 关联项目 ID 数组
- `certifications`：JSONB 证书名称数组

#### 2.2.4 技能更新
- 部分字段更新

#### 2.2.5 技能删除
- 软删除

#### 2.2.6 技能分类列表
- 返回 5 个分类 + 每类技能数量

---

### 2.3 Timeline 子模块（8 个业务功能）

#### 2.3.1 时间线列表查询（带筛选）
- **输入**：event_type 筛选（education/work/project/achievement）、featured 筛选
- **核心逻辑**：
  - 默认排序：`has_featured DESC → start_date DESC`
  - 关联数据一次性加载（避免 N+1）
- **输出**：TimelineEventVO 列表（含 skills/achievements/links 子数组）

#### 2.3.2 时间线详情查询（含全量附属数据）
- skills（从 timeline_event_skills 查）
- achievements（从 timeline_event_achievements 查，按 sort_order 排序）
- links（从 timeline_event_links 查）

#### 2.3.3 时间线事件创建
- **核心逻辑**：
  - `event_type` 枚举校验
  - icon/color：若前端未传，按事件类型自动填充默认值（education→school图标+蓝, work→work图标+绿, project→code图标+红, achievement→events图标+黄）
  - `start_date` 必填 + `end_date` 可选（须 ≥ start_date）
  - **事务范围**：timeline_events + timeline_event_skills + timeline_event_achievements + timeline_event_links

#### 2.3.4 时间线事件更新
- 附属数据全量替换策略：先删旧关联 → 再写入新关联
- **事务范围**：同上四表

#### 2.3.5 时间线事件删除
- 软删除 + 级联删除所有附属关联记录

#### 2.3.6 事件类型列表
- 返回枚举值 `["education","work","project","achievement"]` + 每类事件数量 + 默认图标/颜色

#### 2.3.7 时间线统计
- 总事件数 / 每类型数量 / 进行中事件数（end_date IS NULL）/ featured 数量

#### 2.3.8 进行中事件检测
- `isCurrent`：无 `end_date` 的视为进行中
- 前端使用此标志渲染脉冲动画

---

## 3. 业务规则实现详情

### 3.1 项目状态自动流转
```
状态变更 → "completed"
  ├── end_date 为 null → 自动填充 now()
  └── end_date 已有值 → 保持不变

状态变更 → "in-progress"
  └── end_date 为有值 → 自动置空（表示重新开始）
```

### 3.2 技能经验时长规范
- `experience_years`：非负整数
- `experience_months`：0-11 范围校验
- 存储：分两个字段存储，VO 返回时合并为 `{years, months}` 对象

### 3.3 时间线默认图标/颜色映射
| event_type | 默认 icon | 默认 color |
|-----------|----------|------------|
| education | material-symbols:school | #2A53DD |
| work | material-symbols:work | #51F56C |
| project | material-symbols:code-blocks | #FF4B63 |
| achievement | material-symbols:emoji-events | #FAB83E |

### 3.4 技术栈与标签处理差异
- **技术栈（techStack）**：存储在 project_tech_stacks 关联表，支持按技术名精确搜索
- **标签（tags）**：存储在 project_tags 关联表，用于分类辅助不参与精确搜索
- 两者均采用全量替换策略（更新时先删后写）

### 3.5 关联数据一致性
- 技能的 `projects` JSONB 字段引用项目 ID
- 时间线的 `timeline_event_skills.skill_name` 引用技能名称
- **不强约束外键**（架构要求无外键），由业务层保证一致性：
  - 删除项目时：检查是否被技能引用，提醒用户
  - 删除技能时：检查是否被时间线事件引用

---

## 4. Service 层开发任务

### 4.1 Projects 服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S16 | ProjectDomainService | domain/service/ProjectDomainService.java | 状态流转、技术栈/标签全量替换逻辑、日期校验 |
| S17 | ProjectAppService | application/service/ProjectAppService.java | CRUD 编排 + 关联表事务管理 |
| S18 | TechStackService | domain/service/TechStackService.java | 技术栈聚合去重 + 引用计数 |

### 4.2 Skills 服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S19 | SkillDomainService | domain/service/SkillDomainService.java | 经验时长校验、项目关联一致性 |
| S20 | SkillAppService | application/service/SkillAppService.java | CRUD 编排 |
| S21 | SkillCategoryService | domain/service/SkillCategoryService.java | 分类枚举 + 统计 |

### 4.3 Timeline 服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S22 | TimelineDomainService | domain/service/TimelineDomainService.java | 默认值填充、附属数据管理、进行中检测 |
| S23 | TimelineAppService | application/service/TimelineAppService.java | CRUD 编排 + 附属表事务 |
| S24 | TimelineEventTypeService | domain/service/TimelineEventTypeService.java | 类型枚举 + 默认值 + 统计 |

### 4.4 聚合服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S25 | PortfolioStatService | domain/service/PortfolioStatService.java | 跨子模块聚合统计：项目数+技能数+事件数 |

---

## 5. 对象模型设计

### 5.1 Entity 领域实体

| 实体 | 包路径 | 关键方法 |
|------|--------|---------|
| ProjectEntity | domain/model/entity/ProjectEntity.java | `autoFillEndDate()`, `validateDateRange()` |
| SkillEntity | domain/model/entity/SkillEntity.java | `validateExperience()`, `validateEnumValues()` |
| TimelineEventEntity | domain/model/entity/TimelineEventEntity.java | `isCurrent()`, `fillDefaultIconColor()`, `calculateDuration()` |

### 5.2 PO 持久化对象

| PO | 对应表 |
|----|--------|
| ProjectPO.java | projects |
| ProjectTechStackPO.java | project_tech_stacks |
| ProjectTagPO.java | project_tags |
| SkillPO.java | skills |
| TimelineEventPO.java | timeline_events |
| TimelineEventSkillPO.java | timeline_event_skills |
| TimelineEventAchievementPO.java | timeline_event_achievements |
| TimelineEventLinkPO.java | timeline_event_links |

### 5.3 Repository 接口

| Repository | 包路径 |
|------------|--------|
| ProjectRepository | domain/repository/ProjectRepository.java |
| ProjectTechStackRepository | domain/repository/ProjectTechStackRepository.java |
| ProjectTagRepository | domain/repository/ProjectTagRepository.java |
| SkillRepository | domain/repository/SkillRepository.java |
| TimelineEventRepository | domain/repository/TimelineEventRepository.java |
| TimelineEventSkillRepository | domain/repository/TimelineEventSkillRepository.java |
| TimelineEventAchievementRepository | domain/repository/TimelineEventAchievementRepository.java |
| TimelineEventLinkRepository | domain/repository/TimelineEventLinkRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 项目分页请求 | ProjectPageReq | category, status, featured, techStack 筛选 |
| 项目创建/更新请求 | ProjectSaveReq | 全量字段 |
| 项目 VO | ProjectVO, ProjectDetailVO, ProjectStatVO | |
| 技能分页请求 | SkillPageReq | category, level 筛选 |
| 技能创建/更新请求 | SkillSaveReq | |
| 技能 VO | SkillVO, SkillCategoryVO | |
| 时间线分页请求 | TimelinePageReq | event_type, featured 筛选 |
| 时间线创建/更新请求 | TimelineSaveReq | 含附属数据 |
| 时间线 VO | TimelineEventVO, TimelineStatVO | |
| 聚合统计 VO | PortfolioStatVO | |

---

## 6. 集成点

### 6.1 与 System 模块集成
- **文件上传**：项目封面图片委托给 FileUploadService
- **站点配置**：featurePages 开关控制是否显示项目/技能/时间线页面

### 6.2 与 Content 模块集成
- 无直接集成（技能中 projects JSONB 字段仅为 ID 引用，不做实时关联查询）

### 6.3 内部子模块集成
- Skills ↔ Projects：技能 `projects` JSONB 字段引用项目 ID
- Skills ↔ Timeline：时间线 `timeline_event_skills` 引用技能名称

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | 8 张 portfolio 相关表 | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| SmartValidator | 参数校验 | 已完成 |
| TransactionTemplate | 多表事务 | 框架 |
| FileUploadService（System） | 项目封面图片存储 | 依赖 MOD-06 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| MOD-06 System | 项目/技能/时间线数据 | 站点统计聚合 |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | Projects CRUD + 技术栈/标签关联 + 筛选 | P0 |
| Phase 2 | Skills CRUD + 分类/等级筛选 | P1 |
| Phase 3 | Timeline CRUD + 附属数据管理 | P1 |
| Phase 4 | 聚合统计 + 数据一致性校验 | P2 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 关联表事务一致性 | 主表与关联表在同一事务中 |
| 枚举值校验 | 所有 VARCHAR 枚举字段在 Service 层强校验 |
| 日期约束 | end_date ≥ start_date（业务层校验 + 数据库 CHECK 可选） |
| 软删除级联 | 主表软删除时关联表同步标记或物理删除 |
| N+1 查询避免 | Timeline 附属数据批量查询，非逐条查询 |
