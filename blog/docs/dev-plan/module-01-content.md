# Mizuki 后端开发计划 — Content 模块（文章 + 日记）

> **模块编号**: MOD-01
> **包含子模块**: Posts（文章）、Diary（日记）
> **涉及数据表**: posts, tags, post_tags, categories, archives, diary_entries, comments（部分）
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

Content 模块是 Mizuki 博客系统的**核心内容引擎**，负责管理博客最基础的两类内容实体——**文章（Post）** 与 **日记（Diary）**。这两者共同构成面向访客的内容流，也是 RSS/Atom 订阅、站点统计、搜索功能等下游消费模块的数据源头。

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 文章管理 | 完整 CRUD + 草稿/发布状态流转 + 加密访问控制 + 封面图片管理 |
| 标签体系 | 与文章的 N:N 关联 + 独立 CRUD + slug 自动生成 |
| 分类体系 | 层级分类（parent_id 自引用）+ 独立 CRUD + 图标/封面支持 |
| 归档服务 | 按年月自动聚合并维护 post_ids 快照；
| 日记管理 | 完整 CRUD + 时间线分页 + 多维标签筛选 + 图片关联管理 |
| 全文搜索 | PostgreSQL GIN 索引的 `tsvector` 搜索，覆盖标题 + 正文 |
| 加密访问 | bcrypt 密码哈希存储 + 验证接口 + 短期令牌机制 |

### 1.3 前后端边界

- **前端负责**：Markdown → HTML 渲染、CryptoJS 客户端 AES 解密、分页 UI、标签筛选 UI
- **后端负责**：数据 CRUD、全文搜索 SQL 构造、密码哈希与比对、slug/permalink 唯一性校验、归档维护

---

## 2. 核心业务逻辑实现任务

### 2.1 Posts 子模块（14 个业务功能）

#### 2.1.1 文章列表查询（list/page）
- **输入处理**：分页参数（page/size）、筛选条件（category/tag/lang/draft/encrypted/keyword/dateRange）
- **核心逻辑**：
  - 构造 MyBatis-Flex QueryWrapper，**禁止 JOIN**，通过 post_tags 子查询按标签筛选
  - 默认排序：`has_pinned DESC → priority ASC → published_at DESC`
  - 无认证用户不可见 `has_draft=TRUE` 的文章
  - 全文搜索使用 `to_tsvector('simple', ...) @@ plainto_tsquery('simple', keyword)` 条件
- **输出**：分页后的 PostVO 列表 + PageInfo 元数据

#### 2.1.2 文章详情查询（detail / by-slug）
- **输入处理**：文章 ID（UUID）或 slug/alias/permalink 多策略匹配
- **核心逻辑**：
  - 按 slug → alias → permalink 优先级顺序逐级 fallback 匹配
  - 加密文章：无认证 + 无令牌 → 返回元数据（`content` 字段置 null）+ `passwordHint`
  - 相邻文章计算：在同一排序规则下，分别取前一条/后一条记录
- **输出**：完整 PostVO（含上一页/下一页信息）

#### 2.1.3 文章创建（save）
- **核心逻辑**：
  - 从标题自动生成 slug（小写 → 空格转连字符 → 移除非字母数字 → 截断 100 字符 → 冲突追加 `-N`）
  - permalink/alias 唯一性校验
  - 加密文章：`password` 必填 → bcrypt 哈希 → `password_hash` 入库
  - 标签处理：去重 + trim → 查 tags 表 upsert → 写 post_tags 关联
  - 字数统计：CJK 字符单独计数，非 CJK 按单词计数，排除代码块
  - 归档维护：触发 archives 表的 update/insert（按年月 upsert post_ids）
  - UUID 由业务层生成（`IdGenerator.nextUuid()`）
  - **事务范围**：posts + post_tags + archives 三表写入
- **输出**：新建文章标识 + slug + URL

#### 2.1.4 文章更新（update）
- **核心逻辑**：
  - 部分字段更新（允许只传变更字段）
  - 标签关联：全量替换策略（先删 post_tags WHERE post_id = x → 重新写入）
  - 封面图片：传新图时删旧物理文件
  - 取消加密状态时清除 `password_hash` 和 `password_hint`
  - 更新 `updated_at`（触发器自动处理）
  - slug 变更时需维护归档 zhognd post_ids 关联
  - **事务范围**：posts + post_tags + archives 三表
- **输出**：更新后的完整 PostVO

#### 2.1.5 文章删除（remove）
- **核心逻辑**：
  - 软删除：设置 `deleted_at = now()`
  - 级联清理：删除 post_tags 关联记录 → 清理孤立 tags（无其他文章引用的标签）
  - 归档维护：从 archives 中移除该文章的 post_id
  - 物理清理：删除封面图片文件
- **事务范围**：posts + post_tags + archives

#### 2.1.6 封面图片上传与删除
- **上传处理**：校验格式（jpg/png/webp/gif）→ 校验大小（≤5MB）→ 格式转为 JPG（RGBA→白色背景 RGB）→ 生成唯一文件名 → 存储 → 返回 URL
- **删除处理**：删除物理文件 → 清空 posts.cover_image 字段

#### 2.1.7 分类管理（独立 CRUD）
- categories 表的完整 CRUD
- 层级支持：`parent_id` 自引用，查询时组装树状结构
- 删除时检查：该分类是否还有关联文章，有则拒绝删除或提供迁移选项
- 排序：`sort_order ASC`

#### 2.1.8 标签管理（独立 CRUD）
- tags 表的完整 CRUD
- 创建时自动生成 slug
- 删除时检查是否有文章关联，有则拒绝
- 获取所有标签：去重 + 每个标签的文章数量

#### 2.1.9 归档查询
- 从 archives 表按 `year DESC, month DESC` 查询
- 支持按 category/tag 二次筛选后再重新聚合（无法从 archives 快照满足时实时计算）
- 输出：年份分组 → 每篇文章（id/title/published_date/url/tags）

#### 2.1.10 相关文章推荐
- **标签 Jaccard 相似度**：`|交集| / |并集| × 100`
- **标题 Jaccard 相似度**：分词后同上
- **时间新鲜度**：`30 × e^(-(days_diff / 182))`（6 个月半衰期）
- **同分类加分**：固定 +10
- **总分排序** → 排除自身 + 排除加密文章 → Top N

#### 2.1.11 加密文章密码验证
- bcrypt 比对 `password_hash` → 返回正文 + 签发短期 Token（JWT，有效期 30 分钟）
- Token 可在后续 detail 请求中携带（Header: `X-Unlock-Token`）

#### 2.1.12 草稿/发布状态切换
- `has_draft` 字段翻转
- 发布草稿时：若 `published_at` 为空则自动填充当前时间

#### 2.1.13 批量操作
- 批量删除（removeByIds）
- 批量修改分类/标签

#### 2.1.14 字数统计工具
- 独立工具方法 `WordCountUtils.count(text)` 供创建/更新时调用
- CJK 字符：Unicode 范围 `\u4e00-\u9fff \u3400-\u4dbf` 等逐个计 1
- 非 CJK：按 `\w+` 正则分词计数
- 代码块（```围起来的内容）不计入

---

### 2.2 Diary 子模块（8 个业务功能）

#### 2.2.1 日记列表查询
- 分页 + 按 `entry_date DESC` 排序
- 多维筛选：tags（JSONB `@>` 操作符，多标签 AND 关系）、日期范围（entry_date BETWEEN）
- 仅过滤 `deleted_at IS NULL`

#### 2.2.2 日记详情查询

#### 2.2.3 日记创建
- tags 数组去重、trim、过滤空字符串
- images 数组是已上传图片的 URL（先上传后创建）
- content 必填、最多 5000 字符

#### 2.2.4 日记更新
- 部分字段更新
- images 和 tags 全量替换策略

#### 2.2.5 日记删除
- 软删除 + 清理关联图片物理文件

#### 2.2.6 日记图片上传
- 单张/多张上传、格式校验、大小限制 5MB/张、单次最多 9 张

#### 2.2.7 日记图片删除
- 校验图片是否被其他日记引用 → 物理删除文件

#### 2.2.8 日记标签列表
- 从所有未删除日记的 `tags` JSONB 字段中聚合去重
- 按字母序排列

---

## 3. 业务规则实现详情

### 3.1 Slug 生成规则（Posts）

```
输入: "Hello World! 你好"
→ 小写: "hello world! 你好"
→ 空格/下划线→连字符: "hello-world!-你好"  
→ 移除非允许字符: "hello-world-你好"
→ 合并连续连字符: "hello-world-你好"
→ 截断100字符: "hello-world-你好"
→ 唯一性检查: 若冲突 → "hello-world-你好-2"
→ 空内容fallback: "untitled-{timestamp}"
```

### 3.2 Permalink 解析规则（Posts）
- 支持占位符：`%year%` `%monthnum%` `%day%` `%hour%` `%minute%` `%second%` `%post_id%` `%postname%` `%raw_postname%` `%category%`
- 基于全局站点配置 `permalink_format` 和文章 `published_at` 渲染
- 渲染后存入 `posts.permalink` 字段

### 3.3 加密文章密码规则
- `has_encrypted=TRUE` → `password` 必填
- 存储：bcrypt(12 rounds) → `password_hash`
- 验证：`BCrypt.checkpw(plainPassword, password_hash)`
- 取消加密：`has_encrypted=FALSE` → 同时置空 `password_hash` + `password_hint`

### 3.4 日记标签筛选 AND 逻辑
- 前端传入 `tags=["travel", "food"]`
- SQL: `WHERE tags @> '["travel","food"]'::jsonb`
- 这意味着日记必须同时包含这两个标签

### 3.5 归档维护策略
- 创建文章时：Upsert archives（ON CONFLICT (year, month) DO UPDATE）
- 更新文章时：若 published_at 的年月变化 → 先从旧归档移除 → 再 upsert 新归档
- 删除文章时：从对应归档的 post_ids JSONB 数组中移除该 ID

### 3.6 分类层级管理
- categories.parent_id 自引用
- 查询分类树：先查出所有分类 → 在应用层组装父子关系
- 删除分类约束：若该分类或其子分类下有文章，拒绝删除

---

## 4. Service 层开发任务

### 4.1 Posts 领域服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S01 | PostDomainService | domain/service/PostDomainService.java | 封装文章核心业务逻辑：slug生成、字数统计、标签关联、加密验证、归档维护 |
| S02 | PostAppService | application/service/PostAppService.java | 编排 CRUD 流程、调用 Repository、触发归档更新 |
| S03 | TagDomainService | domain/service/TagDomainService.java | 标签 slug 生成、名称去重、孤立清理 |
| S04 | TagAppService | application/service/TagAppService.java | 标签 CRUD 编排 |
| S05 | CategoryDomainService | domain/service/CategoryDomainService.java | 分类树组装、级联删除校验 |
| S06 | CategoryAppService | application/service/CategoryAppService.java | 分类 CRUD 编排 |
| S07 | ArchiveDomainService | domain/service/ArchiveDomainService.java | 归档 upsert 逻辑、post_ids 维护 |
| S08 | ArchiveAppService | application/service/ArchiveAppService.java | 归档查询编排 |
| S09 | PostRecommendService | domain/service/PostRecommendService.java | 相关文章推荐算法：Jaccard + 时间衰减 + 分类加分 |

### 4.2 Diary 领域服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S10 | DiaryDomainService | domain/service/DiaryDomainService.java | 日记标签处理（去重/trim/上限）、图片引用校验 |
| S11 | DiaryAppService | application/service/DiaryAppService.java | 日记 CRUD 编排、删除时清理图片 |
| S12 | DiaryImageService | domain/service/DiaryImageService.java | 图片引用计数检查、物理文件清理 |

### 4.3 通用内容服务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S13 | SlugService | domain/service/SlugService.java | 通用 slug 生成器（供 posts/tags 复用） |
| S14 | WordCountService | domain/service/WordCountService.java | 多语言字数统计 |
| S15 | SearchService | domain/service/SearchService.java | PostgreSQL 全文搜索查询构建 |

---

## 5. 对象模型设计（Entity / PO / DTO）

### 5.1 Entity 领域实体

| 实体 | 包路径 | 关键方法 |
|------|--------|---------|
| PostEntity | domain/model/entity/PostEntity.java | `generateSlug()`, `isEncrypted()`, `validatePublishable()` |
| TagEntity | domain/model/entity/TagEntity.java | `generateSlug()` |
| CategoryEntity | domain/model/entity/CategoryEntity.java | `isRoot()`, `hasChildren()` |
| ArchiveEntity | domain/model/entity/ArchiveEntity.java | `addPostId()`, `removePostId()`, `getCount()` |
| DiaryEntity | domain/model/entity/DiaryEntity.java | `validateTags()`, `validateImages()` |

### 5.2 PO 持久化对象

| PO | 包路径 | 对应表 |
|----|--------|--------|
| PostPO | infrastructure/persistence/po/PostPO.java | posts |
| TagPO | infrastructure/persistence/po/TagPO.java | tags |
| PostTagPO | infrastructure/persistence/po/PostTagPO.java | post_tags |
| CategoryPO | infrastructure/persistence/po/CategoryPO.java | categories |
| ArchivePO | infrastructure/persistence/po/ArchivePO.java | archives |
| DiaryPO | infrastructure/persistence/po/DiaryPO.java | diary_entries |

### 5.3 Repository 接口

| Repository | 包路径 |
|------------|--------|
| PostRepository | domain/repository/PostRepository.java |
| TagRepository | domain/repository/TagRepository.java |
| PostTagRepository | domain/repository/PostTagRepository.java |
| CategoryRepository | domain/repository/CategoryRepository.java |
| ArchiveRepository | domain/repository/ArchiveRepository.java |
| DiaryRepository | domain/repository/DiaryRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 分页请求 | PostPageReq | page, size, category, tag, lang, keyword, draft, dateRange 筛选 |
| 创建/更新请求 | PostSaveReq | 全量字段 + 部分字段可选 |
| 加密验证请求 | PostPasswordReq | postId + password |
| 文章 VO | PostVO, PostDetailVO, PostListVO | 不同粒度返回 |
| 标签 VO | TagVO, TagStatVO | 含文章计数 |
| 分类 VO | CategoryVO, CategoryTreeVO | 含树状结构 |
| 归档 VO | ArchiveVO, ArchiveYearVO, ArchivePostVO | 按年分组 |
| 日记请求 | DiaryPageReq, DiarySaveReq | |
| 日记 VO | DiaryVO | |

---

## 6. 集成点

### 6.1 与 System 模块集成
- **站点配置**：读取 permalink_format 用于文章 URL 生成
- **文件上传**：封面图片和日记图片的上传委托给 System 模块的 FileUploadService
- **OG 图片生成**：文章发布时触发 OG 图片预生成（异步）
- **RSS/Atom**：读取已发布非加密非草稿文章列表
- **站点统计**：文章总数、分类数、标签数、总字数

### 6.2 与 Social 模块集成
- **评论**：文章详情返回时附带评论数量（调用 Social 模块的 CommentCountService）
- **加密文章**：评论组件根据加密状态隐藏

### 6.3 与 Media 模块集成
- 相册与日记共享图片存储路径规范

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | posts/tags/post_tags/categories/archives/diary_entries 表 | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| BCrypt | 密码哈希 | 系统工具 |
| SmartValidator | 参数校验 | 已完成 |
| TransactionTemplate | 多表事务 | 框架 |
| VirtualTaskManager | 异步归档维护/OG图片生成 | 框架 |
| FileUploadService（System） | 封面/日记图片存储 | 依赖 MOD-06 |
| SystemConfigService（System） | 读取 permalink_format | 依赖 MOD-06 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| MOD-04 Social | PostService（评论数查询） | Comments 需要查询文章是否存在 |
| MOD-06 System | PostService（统计/RSS/OG） | 系统模块消费文章数据 |
| MOD-06 System | 搜索日志记录 | 搜索触发时记录 search_logs |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | Posts CRUD + Slug 生成 + 字数统计 + 标签关联 | P0 |
| Phase 2 | 分类 CRUD + 标签 CRUD + 归档维护 | P0 |
| Phase 3 | 全文搜索 + 相关推荐 + 加密密码验证 | P1 |
| Phase 4 | Diary CRUD + 标签筛选 + 图片管理 | P1 |
| Phase 5 | 封面图片上传/删除 + 批量操作 | P2 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 列表查询性能 | ≤200ms（含全文搜索） |
| Slug 唯一性保障 | 数据库 UNIQUE 约束 + 应用层冲突处理 |
| 加密密码安全 | bcrypt(12) 哈希，永不返回原始密码 |
| 软删除影响 | 所有查询默认过滤 deleted_at IS NULL |
| 标签筛选正确性 | JSONB @> 操作符保证 AND 语义 |
| 归档数据一致性 | 文章增删改时事务性维护 archives 表 |
