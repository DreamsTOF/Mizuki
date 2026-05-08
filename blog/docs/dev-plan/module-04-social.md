# Mizuki 后端开发计划 — Social 模块（友链 + 评论）

> **模块编号**: MOD-04
> **包含子模块**: Friends（友链）、Comments（评论）
> **涉及数据表**: friends, friend_tags, friend_tag_links, comments
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

Social 模块是 Mizuki 博客的**社交互动引擎**，负责两块核心社交功能：友链（Friends）管理与评论（Comments）系统。友链模块展示博客作者的友情链接，评论模块为文章/日记/页面提供读者互动能力。

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 友链管理 | 完整 CRUD + 标签分类筛选 + 排序 + 头像管理（外链/本地上传） + 启用/禁用控制 |
| 评论管理 | 完整 CRUD + 多目标类型支持 + 嵌套回复 + 审核机制 + 点赞计数 + IP/UA 记录 |

### 1.3 前后端边界

- **前端负责**：友链卡片渲染、搜索/筛选交互、评论表单 UI、嵌套回复展示
- **后端负责**：数据 CRUD、审核逻辑、排序、IP/UA 记录

---

## 2. 核心业务逻辑实现任务

### 2.1 Friends 子模块（6 个业务功能）

#### 2.1.1 友链列表查询
- **输入**：分页、排序（默认 sort_order ASC）、标签筛选（通过 friend_tag_links 子查询）、active 状态筛选
- **核心逻辑**：
  - 仅返回 `has_active=TRUE` 且 `deleted_at IS NULL` 的记录（公开接口）
  - 管理员接口可查看全部（含禁用和已删除）
  - 关联查询 friend_tags（通过 friend_tag_links）
- **输出**：FriendVO 列表（含标签数组）

#### 2.1.2 友链详情查询

#### 2.1.3 友链创建
- **核心逻辑**：
  - `siteurl` 全局唯一性校验
  - `img_type`：0=外链图片（imgurl 直接存外链 URL）、1=本地上传（先上传获取 URL → 同时存 img_storage_path）
  - 标签关联：先写 friends → 再写 friend_tag_links
  - 若传入的标签不存在，自动创建 friend_tags 记录
- **事务范围**：friends + friend_tag_links（+ friend_tags 若自动创建）

#### 2.1.4 友链更新
- 部分字段更新 + 标签全量替换
- 若 img_type 从 0 变为 1（上传新头像），清理旧上传文件

#### 2.1.5 友链删除
- 软删除 + 级联删除 friend_tag_links 关联
- 若 img_type=1，清理本地上传的头像文件

#### 2.1.6 友链标签管理
- 标签 CRUD：name 唯一
- 获取所有标签：去重 + 每个标签的友链数量

---

### 2.2 Comments 子模块（6 个业务功能）

#### 2.2.1 评论列表查询（按目标）
- **输入**：target_type（post/diary/page）+ target_id + 分页 + 排序（created_at DESC 或 has_pinned DESC + created_at DESC）
- **核心逻辑**：
  - 公开接口仅返回 `has_approved=TRUE` 的评论
  - 管理员接口可查看待审核评论
  - 嵌套回复：parent_id 为 NULL 的为顶级评论 → 在应用层组装父子树；或一次性查出所有评论后在应用层递归组装
- **输出**：CommentVO 列表（含 replies 子数组或 parent_id 标记由前端组装）

#### 2.2.2 评论创建
- **核心逻辑**：
  - 必填：target_type + target_id + author_name + content
  - 可选：author_email + author_url + ip_address + user_agent
  - parent_id 为 NULL 表示顶级评论，有值表示回复
  - 防刷：同 IP 同目标 60 秒内最多 3 条
  - 默认 has_approved=FALSE（需审核），管理员评论默认 has_approved=TRUE
- **输出**：新创建的 CommentVO

#### 2.2.3 评论审核
- 管理员操作：将 has_approved 从 FALSE 改为 TRUE（或拒绝删除）

#### 2.2.4 评论更新
- 仅允许更新 content 字段（管理员可编辑任何评论内容）

#### 2.2.5 评论删除
- 软删除
- 若该评论有子回复（其他评论的 parent_id 指向此评论），子回复也一并软删除（级联删除评论树）

#### 2.2.6 评论点赞
- like_count + 1（简单计数，不做用户去重；若需要去重则需额外 like_records 表）
- 暂不要求登录/认证即可点赞

---

## 3. 业务规则实现详情

### 3.1 友链头像存储策略
```
img_type = 0（外链）→ imgurl 直接存外部 URL → img_storage_path = NULL
img_type = 1（本地上传）→ 先调 FileUploadService → imgurl = 返回的 URL → img_storage_path = 存储路径
```

### 3.2 评论审核流
```
访客创建评论 → has_approved = FALSE → 进入后台待审列表
管理员审核 → 通过：has_approved = TRUE → 公开可见
           → 拒绝：软删除
管理员评论 → has_approved = TRUE → 直接公开可见
```

### 3.3 评论嵌套规则
- 仅支持一层回复（回复顶级评论或回复其他回复都通过 parent_id 指向被回复的评论）
- 不支持无限嵌套（前端只展平两层）
- 查询时一次性查出目标下所有通过审核的评论 → 应用层组装树/列表

### 3.4 防刷规则
- 同一 IP + 同一 target_id：60 秒内最多 3 条评论
- 评论内容长度：1-2000 字符
- author_name 长度：1-100 字符
- author_email 可选但若提供需格式校验

### 3.5 友链排序
- 按 sort_order ASC，相同 sort_order 按 created_at ASC
- 管理后台可拖拽排序（通过批量更新 sort_order 实现）

---

## 4. Service 层开发任务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S35 | FriendDomainService | domain/service/FriendDomainService.java | 头像上传处理、标签自动创建、排序管理 |
| S36 | FriendAppService | application/service/FriendAppService.java | CRUD 编排 |
| S37 | FriendTagService | domain/service/FriendTagService.java | 标签 CRUD + 统计 |
| S38 | CommentDomainService | domain/service/CommentDomainService.java | 审核流、防刷校验、嵌套回复组装 |
| S39 | CommentAppService | application/service/CommentAppService.java | CRUD 编排 + 反垃圾检测 |
| S40 | CommentCountService | domain/service/CommentCountService.java | 按目标统计评论数（供其他模块查询） |

---

## 5. 对象模型设计

### 5.1 Entity 领域实体

| 实体 | 关键方法 |
|------|---------|
| FriendEntity | `isExternalImage()`, `hasCustomAvatar()`, `isActive()` |
| FriendTagEntity | `validateName()` |
| CommentEntity | `approve()`, `reject()`, `isTopLevel()`, `hasReplies()`, `incrementLike()` |

### 5.2 PO 持久化对象

| PO | 对应表 |
|----|--------|
| FriendPO.java | friends |
| FriendTagPO.java | friend_tags |
| FriendTagLinkPO.java | friend_tag_links |
| CommentPO.java | comments |

### 5.3 Repository

| Repository | 包路径 |
|------------|--------|
| FriendRepository | domain/repository/FriendRepository.java |
| FriendTagRepository | domain/repository/FriendTagRepository.java |
| FriendTagLinkRepository | domain/repository/FriendTagLinkRepository.java |
| CommentRepository | domain/repository/CommentRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 友链请求 | FriendPageReq, FriendSaveReq | |
| 友链 VO | FriendVO, FriendTagVO | |
| 评论请求 | CommentPageReq, CommentSaveReq, CommentAuditReq | |
| 评论 VO | CommentVO, CommentStatVO | |

---

## 6. 集成点

### 6.1 与 System 模块集成
- **文件上传**：友链头像本地上传委托 FileUploadService

### 6.2 与 Content 模块集成
- **评论计数**：文章/日记详情返回时调用 CommentCountService
- **评论开关**：文章 `has_comment_enabled=FALSE` 时拒绝新建评论
- **加密文章**：加密文章的评论默认隐藏或禁用

### 6.3 内部集成
- CommentCountService 提供 `getCommentCount(targetType, targetId)` 方法供外部模块注入

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | friends/friend_tags/friend_tag_links/comments | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| FileUploadService（System） | 友链头像本地上传 | 依赖 MOD-06 |
| PostService（Content） | 验证评论目标存在性 + 评论开关 | 依赖 MOD-01 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| MOD-01 Content | CommentCountService | 文章/日记详情返回评论数 |
| MOD-06 System | CommentCountService | 站点统计聚合 |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | Friends CRUD + 标签管理 + 头像处理 | P1 |
| Phase 2 | Comments CRUD + 审核流 + 嵌套回复 | P1 |
| Phase 3 | 防刷机制 + 点赞去重 + 反垃圾检测 | P2 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 友链 siteurl 唯一性 | 数据库 UNIQUE 约束 + 应用层预检 |
| 评论审核流 | 默认未审核，仅管理员可批量审核 |
| 嵌套查询性能 | 一次性查询目标下所有评论（≤500条），应用层组装 |
| 防刷有效性 | IP + 时间窗口限制 |
| 软删除级联 | 评论删除时级联子回复 |
