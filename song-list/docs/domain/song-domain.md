# 音乐内容领域（Song Domain）需求文档

## 1. 领域概览

| 项目   | 内容         |
| ---- | ---------- |
| 领域名称 | 音乐内容（Song） |
| 聚合根  | Song       |
| 核心职责 | 歌曲CRUD、点击统计、搜索筛选、与标签的多对多关联 |
| 模块路径 | `dreamtof-songs/src/main/java/cn/dreamtof/song` |
| 接口路径前缀 | `/songs`   |

## 2. 领域边界定义

### 2.1 边界内（核心职责）

本领域负责所有与歌曲内容相关的功能：

- **歌曲生命周期管理**：添加、编辑、删除、批量操作
- **歌曲信息管理**：歌曲名、歌手、专辑、语言、跳转链接、状态、付费等级、点歌规则等
- **歌曲点击记录与统计**：记录点击日志、更新点击统计（总点击、今日、本周、本月）
- **搜索与筛选**：按关键词搜索、按语言/标签筛选
- **排序功能**：按添加时间、歌曲名、点击数排序
- **歌曲-标签关联管理**：建立和维护歌曲与标签的多对多关联（通过 `song_label` 表）

### 2.2 边界外（与其他领域的协作）

| 协作领域 | 协作关系 | 说明 |
|---------|---------|------|
| **Auth（身份认证）** | 依赖 | 获取当前登录用户ID用于权限校验和创建者标识 |
| **Playlist（歌单管理）** | 依赖/协作 | 歌曲通过 `playlist_song` 关联表与歌单建立关系；添加歌曲时需指定目标歌单 |
| **Label（标签管理）** | **双向协作** | 使用Label领域提供的标签服务；**监听`LabelDeletedEvent`清理song_label关联** |

### 2.3 数据所有权边界

#### 2.3.1 表归属与维护责任

| 表名 | 归属领域 | 维护责任 | 说明 |
|-----|---------|---------|------|
| `song` | **Song领域** | Song领域 | 歌曲主表，Song聚合根管理 |
| `song_label` | **Song领域** | Song领域 | 歌曲-标签关联表，由Song领域负责创建和清理 |
| `song_click_stat` | **Song领域** | Song领域 | 点击统计表，由Song领域维护 |
| `song_click_log` | **Song领域** | Song领域 | 点击日志表，由Song领域写入 |
| `playlist_song` | **Playlist领域** | Playlist领域（但由Song领域触发创建/删除） | 歌单-歌曲关联表，物理记录由Song领域在添加/删除歌曲时创建/删除 |

#### 2.3.2 外键约束与引用完整性

| 字段 | 引用表 | 引用字段 | 领域维护方 | 说明 |
|-----|-------|---------|-----------|------|
| `song.playlist_id` | `playlist` | `id` | **Playlist领域** | 歌单ID，由Playlist领域保证存在性 |
| `song.created_by` | `user` | `id` | **Auth领域** | 创建人ID，由Auth领域保证用户存在 |
| `song_label.song_id` | `song` | `id` | **Song领域** | 歌曲ID外键，Song领域保证 |
| `song_label.label_id` | `label` | `id` | **Label领域** | 标签ID外键，Label领域保证 |
| `song_click_stat.song_id` | `song` | `id` | **Song领域** | 歌曲ID外键，Song领域保证 |
| `song_click_log.song_id` | `song` | `id` | **Song领域** | 歌曲ID外键，Song领域保证 |

**说明**：
- 虽然 `playlist_song` 表归属于 Playlist 领域，但其物理记录的**创建和删除操作由 Song 领域触发**（添加歌曲时创建、删除歌曲时删除）。
- 所有外键约束应在数据库层面通过 FK 约束或应用层逻辑保证（建议数据库层FK）。

### 2.4 领域事件边界

#### 2.4.1 发布的事件

| 事件名称 | 事件类型 | 触发时机 | 消费者领域 | 事件数据 | 说明 |
|---------|---------|---------|-----------|---------|------|
| `SongCreatedEvent` | 领域事件 | 歌曲创建成功后 | 可选（通知、统计等） | `songId`, `name`, `createdBy`, `playlistId` | 歌曲创建事件，用于异步通知 |
| `SongDeletedEvent` | **领域事件（必须）** | **歌曲软删除成功后** | **Playlist领域** | `songId`, `playlistId` | **通知Playlist领域解除歌单关联、刷新缓存** |
| `SongClickedEvent` | 领域事件 | 歌曲点击记录成功后 | 统计服务 | `songId`, `userId`, `clickTime` | 触发实时统计更新 |

#### 2.4.2 消费的事件

| 事件名称 | 事件来源 | 处理逻辑 | 幂等性 | 说明 |
|---------|---------|---------|--------|------|
| `LabelDeletedEvent` | Label领域 | 删除 `song_label` 表中所有该标签的关联记录 | **是**，基于 `labelId` 幂等 | 标签删除后清理歌曲关联 |

#### 2.4.3 事件处理规范

- **发布时机**：必须在领域操作**成功提交事务后**发布（避免数据不一致）
- **幂等性**：所有事件消费者必须支持幂等处理（通过业务键去重）
- **事件数据**：仅包含必要信息，避免传输大对象
- **失败重试**：事件发布失败应重试，最终一致性

### 2.5 服务调用边界

#### 2.5.1 Song领域对外调用（依赖其他领域）

| 调用目标 | 服务方法 | 调用时机 | 参数 | 返回值 | 说明 |
|---------|---------|---------|------|--------|------|
| **Label领域** | `LabelService.listAvailableLabels()` | 添加/编辑歌曲时获取标签列表 | 无 | `List<LabelDTO>` | 获取可选标签列表 |
| **Label领域** | `LabelService.createCustomLabel(String name, UUID userId)` | 用户输入新标签时 | 标签名、用户ID | `LabelDTO` | 创建用户自定义标签 |
| **Playlist领域** | `PlaylistService.getByUserId(UUID userId)` | 添加歌曲时获取用户歌单列表 | 用户ID | `List<PlaylistDTO>` | 校验歌单归属和权限 |
| **Playlist领域** | `PlaylistService.validateOwnership(UUID playlistId, UUID userId)` | 添加歌曲时校验歌单权限 | 歌单ID、用户ID | `boolean` | 校验当前用户是否有权操作该歌单 |
| **Auth领域** | `UserService.exists(UUID userId)` | 权限校验时 | 用户ID | `boolean` | 校验用户是否存在（防御性） |

#### 2.5.2 Song领域对外提供（被其他领域调用）

| 暴露服务 | 调用领域 | 场景 | 说明 |
|---------|---------|------|------|
| `SongService.getBySongIds(List<UUID> songIds)` | Playlist领域 | 歌单详情页批量查询歌曲信息 | 提供歌曲基础信息查询 |
| `SongService.getBasicInfo(UUID songId)` | 其他领域（通用） | 其他领域需要歌曲基本信息 | 只返回核心字段，避免N+1查询 |

**注意**：Song领域**不向外部提供完整的歌曲编辑/删除权限校验服务**，外部领域应通过领域事件或直接查询数据库状态来感知变化。

### 2.6 权限校验边界

#### 2.6.1 歌曲所有权校验

| 操作 | 校验规则 | 错误码 | 说明 |
|-----|---------|--------|------|
| 编辑歌曲 | `song.createdBy == currentUserId` | `PERMISSION_DENIED` | 只有创建者可编辑 |
| 删除歌曲 | `song.createdBy == currentUserId` | `PERMISSION_DENIED` | 只有创建者可删除 |
| 批量操作 | 所有选中歌曲的 `createdBy` 均为 `currentUserId` | `PERMISSION_DENIED` | 批量操作只能操作自己的歌曲 |

#### 2.6.2 歌单权限校验

| 操作 | 校验规则 | 错误码 | 说明 |
|-----|---------|--------|------|
| 添加歌曲到歌单 | `playlist.ownerId == currentUserId` | `PERMISSION_DENIED` | 只有歌单所有者才能添加歌曲 |

#### 2.6.3 歌曲状态可见性

| 状态 | 可见性规则 | 说明 |
|-----|-----------|------|
| `AVAILABLE` (1) | 所有人可见 | 可点状态，公开显示 |
| `LEARNING` (2) | 所有人可见 | 学习中状态，公开显示但不允许点播（根据规则） |
| `REMOVED` (3) | **仅创建者可见** | 暂不可点状态，在歌单中隐藏或置灰显示 |

**注意**：歌曲删除是软删除（`deleted=1`），软删除的数据**对所有用户不可见**，仅创建者在"已删除"回收站可见（如有此功能）。

### 2.7 软删除边界

#### 2.7.1 删除策略

- **软删除字段**：`song.deleted`（0-未删除，1-已删除）
- **删除操作**：更新 `deleted=1`，不物理删除记录
- **查询过滤**：所有查询必须附加 `deleted = 0` 条件

#### 2.7.2 同步事务内级联清理（必须）

当删除歌曲（`song.deleted = 1`）时，在**同一数据库事务内**同步清理以下关联数据：

| 表名 | 清理方式 | 说明 |
|-----|---------|------|
| `song_label` | **物理删除** | 删除所有 `song_id` 关联记录 |
| `playlist_song` | **物理删除** | 删除所有 `song_id` 关联记录（即使Playlist领域维护） |
| `song_click_stat` | **物理删除或保留** | 建议删除，统计无意义；也可保留用于历史分析 |

#### 2.7.3 异步清理（可选）

| 表名 | 清理方式 | 说明 |
|-----|---------|------|
| `song_click_log` | **不清理，永久保留** | 点击日志用于长期分析和审计，永不删除 |

#### 2.7.4 领域事件发布

删除歌曲后**必须发布** `SongDeletedEvent` 领域事件，原因：
- **通知Playlist领域**：虽然 `playlist_song` 已同步删除，但Playlist领域可能缓存了歌单歌曲列表，需要事件触发缓存失效或视图更新
- **通知其他下游服务**：如推荐系统、搜索索引等需要同步更新

### 2.8 乐观锁边界

- **实体**：`Song` 聚合根
- **字段**：`version`（整型，初始0）
- **使用场景**：编辑歌曲、批量修改状态
- **并发控制**：
  - 更新时 `WHERE id = ? AND version = ?`
  - 影响行数为0则抛出 `OptimisticLockingFailureException`
  - 前端提示"数据已被修改，请刷新后重试"

### 2.9 关联关系维护边界

#### 2.9.1 `song_label`（歌曲-标签关联）

| 操作 | 触发方 | 维护责任 | 说明 |
|-----|-------|---------|------|
| 创建关联 | Song领域（添加/编辑歌曲） | **Song领域** | 在歌曲保存时创建标签关联 |
| 删除关联 | Song领域（删除歌曲） | **Song领域** | 同步删除（事务内） |
| 删除关联 | Label领域（删除标签） | **Song领域（监听事件）** | 异步消费 `LabelDeletedEvent` 清理 |

**维护责任归属**：**Song领域**（Song聚合根负责管理 `labels` 集合）

#### 2.9.2 `playlist_song`（歌单-歌曲关联）

| 操作 | 触发方 | 维护责任 | 说明 |
|-----|-------|---------|------|
| 创建关联 | Song领域（添加歌曲） | **Song领域** | 歌曲添加时自动关联到指定歌单 |
| 删除关联 | Song领域（删除歌曲） | **Song领域** | 同步删除（事务内） |
| 删除关联 | Playlist领域（删除歌单） | **Playlist领域** | 歌单删除时Playlist领域清理 |

**维护责任归属**：**Song领域**（歌曲添加/删除是主流程，由Song触发）

### 2.10 点击统计边界

#### 2.10.1 统计表维护

| 表名 | 维护方 | 更新方式 | 说明 |
|-----|-------|---------|------|
| `song_click_stat` | **Song领域** | 异步更新 | 记录 total/today/week/month 点击统计 |
| `song_click_log` | **Song领域** | 只插入 | 点击日志永久保留 |

#### 2.10.2 点击记录接口

- **接口**：`POST /songs/{id}/click`
- **异步性**：前端异步调用，不阻塞页面跳转
- **事务内操作**：
  1. 插入 `song_click_log`（记录日志）
  2. 更新 `song_click_stat`（原子操作：`today_clicks = today_clicks + 1` 等）
  3. 更新 `song.total_clicks`（冗余字段，原子操作）
- **并发安全**：使用乐观锁或数据库原子操作（`UPDATE ... SET clicks = clicks + 1`）
- **数据保留**：
  - `song_click_log`：永久保留，用于数据分析
  - `song_click_stat`：长期保留，定期归档历史数据

#### 2.10.3 统计维度

| 字段 | 含义 | 更新频率 | 重置策略 |
|-----|------|---------|---------|
| `total_clicks` | 总点击次数 | 每次点击+1 | 不重置 |
| `today_clicks` | 今日点击次数 | 每次点击+1 | 每日00:00重置为0 |
| `week_clicks` | 本周点击次数 | 每次点击+1 | 每周一00:00重置为0 |
| `month_clicks` | 本月点击次数 | 每次点击+1 | 每月1日00:00重置为0 |
| `last_click_time` | 最后点击时间 | 每次点击更新 | 不重置 |

**重置任务**：需定时任务（如 Quartz）在每日/每周/每月初重置对应计数器。

### 2.11 搜索和筛选边界

#### 2.11.1 搜索能力

| 搜索类型 | 搜索字段 | 索引 | 说明 |
|---------|---------|------|------|
| 全文搜索 | `name`, `singer`, `album` | 联合索引 `idx_song_name_singer` | 支持关键词模糊匹配（LIKE %keyword%） |
| 标签筛选 | `song_label` + `label` 表联表 | `idx_song_label_song_id`, `idx_song_label_label_id` | 多标签取并集 |
| 语言筛选 | `language` | `idx_song_language`（建议创建） | 多语言取并集 |

#### 2.11.2 筛选规则

- **标签筛选**：多选标签条件时，取**并集**（歌曲包含任一选中标签即匹配）
- **语言筛选**：多选语言条件时，取**并集**
- **组合规则**：不同筛选条件（语言、标签、状态）之间取**交集**

#### 2.11.3 跨领域协作

- **标签信息获取**：筛选标签时需调用 `LabelService.listAvailableLabels()` 获取标签元数据
- **状态过滤**：根据 `SongStatusEnum` 值过滤，`REMOVED` 状态默认不显示

### 2.12 与其他领域的数据流转

#### 2.12.1 Label → Song（标签删除清理）

```
Label领域                          Song领域
   │                                   │
   │  1. 删除标签（软删除）              │
   │  发布：LabelDeletedEvent           │
   │───────────────────────────────────>│
   │                                   │
   │                                   │  2. 监听事件，处理幂等
   │                                   │   DELETE FROM song_label WHERE label_id = ?
   │                                   │
   │<───────────────────────────────────│
   │  3. 处理完成（可选ACK）             │
```

#### 2.12.2 Song → Playlist（歌曲删除通知）

```
Song领域                            Playlist领域
   │                                   │
   │  1. 删除歌曲（软删除）              │
   │  同步清理：playlist_song           │
   │  发布：SongDeletedEvent            │
   │───────────────────────────────────>│
   │                                   │
   │                                   │  2. 监听事件，处理幂等
   │                                   │   - 清理Playlist缓存
   │                                   │   - 更新歌单歌曲计数
   │                                   │
   │<───────────────────────────────────│
   │  3. 处理完成（可选ACK）             │
```

#### 2.12.3 Song → Auth（用户校验）

```
Song领域                            Auth领域
   │                                   │
   │  1. 编辑/删除歌曲前校验             │
   │  调用：UserService.exists(userId)  │
   │───────────────────────────────────>│
   │                                   │
   │                                   │  2. 查询用户是否存在
   │                                   │
   │<───────────────────────────────────│
   │  3. 返回 true/false                │
```

#### 2.12.4 Playlist → Song（歌单校验）

```
Playlist领域                        Song领域
   │                                   │
   │  1. 添加歌曲到歌单                  │
   │  调用：PlaylistService.validate... │
   │───────────────────────────────────>│
   │                                   │
   │                                   │  2. 校验歌单存在性、权限
   │                                   │
   │<───────────────────────────────────│
   │  3. 返回校验结果                    │
```

## 3. 核心领域模型

### 3.1 聚合根：Song（歌曲）

```
Song (聚合根)
├── id: UUID                      # 歌曲唯一标识（主键）
├── name: String                  # 歌曲名称（必填）
├── singer: String                # 歌手名称
├── album: String                 # 专辑名称
├── language: String              # 语言（中/英/日/韩/其他）
├── jumpUrl: String               # 第三方平台跳转链接
├── remark: String                # 备注信息
├── status: SongStatusEnum        # 歌曲状态（1-可点，2-学习中，3-暂不可点）
├── paidLevel: PaidLevelEnum      # 付费等级（0-免费，1-初级，2-中级，3-高级）
├── rule: SongRuleEnum            # 点歌规则（0-无，1-需关注，2-需粉丝牌...）
├── coverUrl: String              # 歌曲封面图URL
├── totalClicks: Integer          # 总点击次数（冗余字段，用于快速查询）
├── version: Integer              # 乐观锁版本号
├── createdBy: UUID               # 创建人ID
├── updatedBy: UUID               # 更新人ID
├── createTime: DateTime          # 创建时间
├── updateTime: DateTime          # 更新时间
├── deleted: Integer              # 删除标记（0-未删除，1-已删除）
└── labels: List<SongLabel>       # 歌曲-标签关联（实体集合）
```

### 3.2 实体：SongLabel（歌曲-标签关联）

```
SongLabel (实体)
├── id: UUID                      # 关联记录ID（主键）
├── songId: UUID                  # 歌曲ID
├── labelId: UUID                 # 标签ID
├── createdBy: UUID               # 创建人ID
└── createTime: DateTime          # 创建时间
```

**说明**：`song_label` 为关联表，删除标签关联时直接删除记录，不需要软删除和版本号。

### 3.3 实体：SongClickLog（点击日志）

```
SongClickLog (实体)
├── id: UUID                      # 日志ID（主键）
├── songId: UUID                  # 歌曲ID
├── userId: UUID                  # 点击用户ID（未登录为NULL）
├── ipAddress: String             # IP地址
├── userAgent: String             # 用户代理（浏览器/设备信息）
├── referer: String               # 来源页面
└── createTime: DateTime          # 创建时间
```

**说明**：`song_click_log` 为日志表，只插入不更新，不需要更新时间、创建人、版本号等字段。

### 3.4 实体：SongClickStat（点击统计）

```
SongClickStat (实体)
├── id: UUID                      # 统计记录ID（主键）
├── songId: UUID                  # 歌曲ID（唯一索引）
├── totalClicks: Integer          # 总有效点击次数
├── todayClicks: Integer          # 今日点击次数
├── weekClicks: Integer           # 本周点击次数
├── monthClicks: Integer          # 本月点击次数
├── lastClickTime: DateTime       # 最后点击时间
└── updateTime: DateTime          # 更新时间
```

**说明**：`song_click_stat` 为统计表，只更新统计数据，不需要创建人、版本号等字段。

### 3.5 值对象

| 值对象 | 说明 | 校验规则 |
|-------|------|---------|
| SongName | 歌曲名称 | 必填，1-100字符 |
| Singer | 歌手 | 可选，最大100字符 |
| Album | 专辑 | 可选，最大100字符 |
| Language | 语言 | 归一化处理（中/英/日/韩/其他） |
| JumpUrl | 跳转链接 | URL格式，可选，最大255字符 |
| Remark | 备注 | 可选，最大500字符 |

### 3.6 枚举定义

```java
/**
 * 歌曲状态枚举
 * 对应 song 表 status 字段
 */
public enum SongStatusEnum {
    AVAILABLE(1, "可点"),      // 默认可点
    LEARNING(2, "学习中"),     // 学习中
    UNAVAILABLE(3, "暂不可点"); // 暂不可点
}

/**
 * 付费等级枚举
 * 对应 song 表 paid_level 字段
 */
public enum PaidLevelEnum {
    FREE(0, "免费"),           // 免费
    PRIMARY(1, "初级"),        // 初级
    INTERMEDIATE(2, "中级"),   // 中级
    ADVANCED(3, "高级");       // 高级
}

/**
 * 点歌规则枚举
 * 对应 song 表 rule 字段（单值存储，MVP只支持单选）
 */
public enum SongRuleEnum {
    NONE(0, "无"),             // 无需特殊规则
    FOLLOW(1, "需关注"),       // 需关注主播
    MEDAL(2, "需粉丝牌"),      // 需粉丝牌
    CAPTAIN(3, "需舰长及以上"), // 需舰长及以上
    GOVERNOR(4, "需提督及以上"), // 需提督及以上
    VICEROY(5, "需总督及以上"),  // 需总督及以上
    SC(6, "需付费");           // 需付费/SC
}
```

## 4. 功能需求

### 4.1 添加歌曲

- **功能描述**：主播手动添加单首歌曲到指定歌单
- **前置条件**：主播已登录
- **主流程**：
  1. 主播点击"添加歌曲"按钮，打开弹窗表单
  2. 填写歌曲信息（歌曲名必填，其他可选）
  3. 选择目标歌单（从主播的歌单列表中选择）
  4. 选择标签（可选，可多选）
  5. 点击"保存"提交，系统校验必填项
  6. 后端处理：
     - 创建歌曲记录，`created_by` 自动设为当前登录用户
     - 在 `playlist_song` 关联表创建记录，关联到指定歌单
     - 初始化歌曲点击统计记录，所有计数为0
     - 处理标签关联：在 `song_label` 表创建关联记录
  7. 保存成功后关闭弹窗，歌单列表自动刷新展示新添加的歌曲
- **异常流程**：
  - 必填项为空：对应字段下方提示"请输入XXX"
  - URL格式错误：提示"请输入正确的URL地址"
  - 歌单不存在或无权限：提示"无权操作该歌单"

### 4.2 编辑歌曲

- **功能描述**：主播修改已添加歌曲的信息
- **前置条件**：主播已登录，且为歌曲创建者
- **主流程**：
  1. 主播在歌单列表悬停在某行歌曲上，显示编辑按钮
  2. 点击编辑，打开表单并填充当前歌曲数据
  3. 修改后提交，后端校验版本号
  4. 版本号匹配则更新歌曲信息，版本号不匹配返回冲突错误
  5. 同步更新标签关联（新增/删除）
- **异常流程**：
  - 权限不足：非歌曲创建者尝试编辑，后端返回403错误
  - 版本冲突：提示"数据已被修改，请刷新后重试"

### 4.3 删除歌曲

- **功能描述**：主播从歌单中移除歌曲（软删除）
- **前置条件**：主播已登录，且为歌曲创建者
- **主流程**：
  1. 主播在歌单列表悬停在某行歌曲上，显示删除按钮
  2. 点击删除，弹出二次确认对话框
  3. 确认删除，后端执行软删除（数据库事务保证一致性）：
     - 标记歌曲记录 `deleted = 1`
     - 删除 `playlist_song` 关联记录
     - 删除 `song_label` 关联记录
     - **发布 `SongDeletedEvent` 领域事件**
  4. 删除成功，列表自动刷新
- **说明**：
  - 歌曲采用软删除，保留历史数据
  - 删除时同步清理关联数据
  - **通过领域事件通知Playlist领域解除歌单关联**

### 4.4 批量删除歌曲

- **功能描述**：主播批量删除多首歌曲
- **前置条件**：主播已登录，且为歌曲创建者
- **主流程**：
  1. 主播在歌单列表勾选多行歌曲
  2. 点击批量删除按钮，弹出二次确认对话框
  3. 确认删除，后端批量执行软删除（数据库事务保证一致性）
  4. **为每首删除的歌曲发布 `SongDeletedEvent` 领域事件**
  5. 删除成功，列表自动刷新，清空勾选状态

### 4.5 批量修改歌曲状态

- **功能描述**：主播批量修改多首歌曲的状态
- **前置条件**：主播已登录，且为歌曲创建者
- **主流程**：
  1. 主播在歌单列表勾选多行歌曲
  2. 点击批量修改状态按钮，弹出状态选择对话框
  3. 选择目标状态（可点/学习中/暂不可点）
  4. 确认修改，后端批量执行更新
  5. 修改成功，列表自动刷新，清空勾选状态

### 4.6 歌曲点击记录

- **功能描述**：记录用户点击歌曲跳转链接的行为
- **前置条件**：用户（登录或未登录）点击歌曲卡片或跳转按钮
- **主流程**：
  1. 用户点击歌曲卡片上的跳转链接按钮
  2. 前端发送点击事件到后端（异步POST请求，不阻塞跳转）
  3. 后端处理（数据库事务）：
     - 记录点击日志：`song_click_log` 表插入记录（song_id、user_id、IP地址、User-Agent、referer）
     - 更新点击统计：`song_click_stat` 表更新（total_clicks +1，today_clicks +1，last_click_time 更新）
     - 更新歌曲冗余字段：`song.total_clicks` +1
  4. 返回成功响应，前端跳转到第三方链接
- **说明**：
  - 点击日志永久保留，用于长期数据分析和审计
  - 统计更新采用乐观锁或原子操作保证并发安全

### 4.7 搜索功能

- **功能描述**：按关键词搜索歌曲
- **前置条件**：用户已登录（主播）或访问公开歌单（普通用户）
- **主流程**：
  1. 用户在搜索框输入关键词
  2. 点击搜索按钮或按回车键触发搜索
  3. 系统匹配歌曲名、歌手名、专辑名中包含关键词的歌曲
  4. 列表展示匹配结果
- **筛选条件组合规则**：搜索关键词 与 其他筛选条件（语言/标签）之间取**交集**

### 4.8 筛选功能

- **功能描述**：按语言、标签多条件筛选歌曲
- **筛选规则**：
  - **语言筛选**：多选语言条件（中/英/日/韩/其他）
  - **标签筛选**：多选标签条件，取并集（包含任一选中标签的歌曲都显示）
  - **组合规则**：多个筛选条件（语言/标签）之间取**交集**；同一类型的多选条件取**并集**

### 4.9 排序功能

- **功能描述**：对歌曲列表进行排序
- **排序方式**：
  - 按添加时间（最新优先 - 降序）
  - 按添加时间（最早优先 - 升序）
  - 按歌曲名（A-Z 升序）
  - 按点击数（热门优先 - 降序）

### 4.10 监听标签删除事件（领域事件消费）

- **功能描述**：监听 `LabelDeletedEvent` 事件，清理被删除标签的歌曲关联
- **事件来源**：Label领域发布
- **处理逻辑**：
  1. 监听 `LabelDeletedEvent` 领域事件
  2. 从事件中获取被删除的标签ID
  3. 删除 `song_label` 表中所有该标签的关联记录
- **业务规则**：
  - 标签删除后，相关歌曲不再显示该标签
  - 清理操作幂等，可重复执行

## 5. 与其他领域的协作关系

### 5.1 与 Playlist 领域的协作

```
Song领域                    Playlist领域
   │                              │
   │  1. 添加歌曲到歌单            │
   │  请求：songId, playlistId     │
   │─────────────────────────────>│
   │                              │
   │                              │  2. 校验歌单存在性和权限
   │                              │
   │<─────────────────────────────│
   │  3. 返回关联结果              │
   │                              │
   │  4. 创建 playlist_song 关联   │
   │     （在Song领域Repository中） │
```

### 5.2 与 Label 领域的协作

```
Song领域                    Label领域
   │                              │
   │  1. 添加/编辑歌曲，包含标签列表 │
   │─────────────────────────────>│
   │                              │
   │                              │  2. 校验标签存在性
   │                              │     - 系统预设标签
   │                              │     - 用户自定义标签
   │                              │
   │                              │  3. 不存在则创建新标签
   │                              │     (is_system=false)
   │                              │
   │<─────────────────────────────│
   │  4. 返回标签ID列表            │
   │                              │
   │  5. 创建 song_label 关联      │
   │     （在Song领域Repository中） │
```

## 6. 接口清单

### 6.1 歌曲管理接口

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 添加歌曲 | POST | `/songs/save` | 创建歌曲并关联到歌单 |
| 编辑歌曲 | PUT | `/songs/update` | 更新歌曲信息（含乐观锁） |
| 删除歌曲 | DELETE | `/songs/remove/{id}` | 删除歌曲及其关联数据（软删除） |
| 批量删除歌曲 | DELETE | `/songs/removeByIds` | 批量删除歌曲 |
| 批量修改状态 | PUT | `/songs/batch/status` | 批量修改歌曲状态 |
| 获取歌曲详情 | GET | `/songs/detail/{id}` | 获取歌曲详细信息 |
| 获取歌曲列表 | GET | `/songs/list` | 获取歌曲列表（不分页） |
| 分页查询歌曲 | POST | `/songs/page` | 分页查询歌曲 |

### 6.2 点击统计接口

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 记录点击 | POST | `/songs/{id}/click` | 记录歌曲点击并更新统计 |
| 获取点击统计 | GET | `/songs/{id}/stats` | 获取歌曲点击统计 |

### 6.3 搜索筛选接口

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 搜索歌曲 | POST | `/songs/search` | 按关键词搜索歌曲 |
| 筛选歌曲 | POST | `/songs/filter` | 按条件筛选歌曲 |

## 7. 数据库表结构

### 7.1 歌曲表（song）

```sql
CREATE TABLE song (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    name VARCHAR(100) NOT NULL,
    singer VARCHAR(100) DEFAULT NULL,
    album VARCHAR(100) DEFAULT NULL,
    language VARCHAR(20) DEFAULT NULL,
    jump_url VARCHAR(255) DEFAULT NULL,
    remark TEXT DEFAULT NULL,
    status INT DEFAULT 1,
    paid_level INT DEFAULT 0,
    rule INT DEFAULT 0,
    cover_url VARCHAR(255) DEFAULT NULL,
    total_clicks INT DEFAULT 0,
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID DEFAULT NULL,
    updated_by UUID DEFAULT NULL,
    deleted INT DEFAULT 0,
    version INT DEFAULT 0
);

COMMENT ON TABLE song IS '歌曲信息表';
COMMENT ON COLUMN song.id IS 'ID';
COMMENT ON COLUMN song.name IS '歌曲名称';
COMMENT ON COLUMN song.singer IS '歌手名称';
COMMENT ON COLUMN song.album IS '专辑名称';
COMMENT ON COLUMN song.language IS '语言：中/英/日/韩/其他';
COMMENT ON COLUMN song.jump_url IS '第三方平台跳转链接';
COMMENT ON COLUMN song.remark IS '备注信息';
COMMENT ON COLUMN song.status IS '歌曲状态：1-可点，2-学习中，3-暂不可点';
COMMENT ON COLUMN song.paid_level IS '付费等级：0-免费，1-初级，2-中级，3-高级';
COMMENT ON COLUMN song.rule IS '点歌规则：0-无，1-需关注，2-需粉丝牌，3-需舰长及以上，4-需提督及以上，5-需总督及以上，6-需付费';
COMMENT ON COLUMN song.cover_url IS '歌曲封面图URL';
COMMENT ON COLUMN song.total_clicks IS '总点击次数（冗余字段）';
COMMENT ON COLUMN song.create_time IS '创建时间';
COMMENT ON COLUMN song.update_time IS '更新时间';
COMMENT ON COLUMN song.created_by IS '创建人 ID';
COMMENT ON COLUMN song.updated_by IS '更新人 ID';
COMMENT ON COLUMN song.deleted IS '删除标记：0-未删除，1-已删除';
COMMENT ON COLUMN song.version IS '版本号，用于乐观锁';

CREATE INDEX idx_song_created_by ON song(created_by);
CREATE INDEX idx_song_status ON song(status);
CREATE INDEX idx_song_create_time ON song(create_time);
CREATE INDEX idx_song_total_clicks ON song(total_clicks);
CREATE INDEX idx_song_name_singer ON song(name, singer);

-- 外键约束（建议创建）
ALTER TABLE song ADD CONSTRAINT fk_song_playlist
    FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE SET NULL;
ALTER TABLE song ADD CONSTRAINT fk_song_creator
    FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE SET NULL;
```

### 7.2 歌曲标签关联表（song_label）

```sql
CREATE TABLE song_label (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    song_id UUID NOT NULL,
    label_id UUID NOT NULL,
    created_by UUID DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE song_label IS '歌曲标签关联表';
COMMENT ON COLUMN song_label.id IS 'ID';
COMMENT ON COLUMN song_label.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_label.label_id IS '标签 ID';
COMMENT ON COLUMN song_label.created_by IS '创建人 ID';
COMMENT ON COLUMN song_label.create_time IS '创建时间';

CREATE INDEX idx_song_label_song_id ON song_label(song_id);
CREATE INDEX idx_song_label_label_id ON song_label(label_id);

-- 外键约束
ALTER TABLE song_label ADD CONSTRAINT fk_song_label_song
    FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE;
ALTER TABLE song_label ADD CONSTRAINT fk_song_label_label
    FOREIGN KEY (label_id) REFERENCES label(id) ON DELETE CASCADE;
```

### 7.3 歌曲点击日志表（song_click_log）

```sql
CREATE TABLE song_click_log (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    song_id UUID NOT NULL,
    user_id UUID DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL,
    user_agent TEXT DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    referer VARCHAR(255) DEFAULT NULL
);

COMMENT ON TABLE song_click_log IS '歌曲点击日志表';
COMMENT ON COLUMN song_click_log.id IS 'ID';
COMMENT ON COLUMN song_click_log.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_click_log.user_id IS '点击用户 ID（未登录为 NULL）';
COMMENT ON COLUMN song_click_log.ip_address IS 'IP 地址';
COMMENT ON COLUMN song_click_log.user_agent IS '用户代理（浏览器/设备信息）';
COMMENT ON COLUMN song_click_log.create_time IS '创建时间';
COMMENT ON COLUMN song_click_log.referer IS '来源页面';

CREATE INDEX idx_click_log_song_id ON song_click_log(song_id);
CREATE INDEX idx_click_log_user_id ON song_click_log(user_id);
CREATE INDEX idx_click_log_create_time ON song_click_log(create_time);

-- 外键约束
ALTER TABLE song_click_log ADD CONSTRAINT fk_click_log_song
    FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE;
```

### 7.4 歌曲点击统计表（song_click_stat）

```sql
CREATE TABLE song_click_stat (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    song_id UUID NOT NULL,
    total_clicks INT DEFAULT 0,
    today_clicks INT DEFAULT 0,
    week_clicks INT DEFAULT 0,
    month_clicks INT DEFAULT 0,
    last_click_time TIMESTAMP DEFAULT NULL,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE song_click_stat IS '歌曲点击统计表';
COMMENT ON COLUMN song_click_stat.id IS 'ID';
COMMENT ON COLUMN song_click_stat.song_id IS '歌曲 ID';
COMMENT ON COLUMN song_click_stat.total_clicks IS '总有效点击次数';
COMMENT ON COLUMN song_click_stat.today_clicks IS '今日点击次数';
COMMENT ON COLUMN song_click_stat.week_clicks IS '本周点击次数';
COMMENT ON COLUMN song_click_stat.month_clicks IS '本月点击次数';
COMMENT ON COLUMN song_click_stat.last_click_time IS '最后点击时间';
COMMENT ON COLUMN song_click_stat.update_time IS '更新时间';

CREATE UNIQUE INDEX uk_stat_song_id ON song_click_stat(song_id);

-- 外键约束
ALTER TABLE song_click_stat ADD CONSTRAINT fk_click_stat_song
    FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE;
```

### 7.5 歌单-歌曲关联表（playlist_song）- 在Playlist领域维护

```sql
-- 注：此表属于 Playlist 领域，此处仅展示结构供参考
CREATE TABLE playlist_song (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    playlist_id UUID NOT NULL,
    song_id UUID NOT NULL,
    added_by UUID DEFAULT NULL,      -- 添加人（歌曲创建者）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE playlist_song IS '歌单歌曲关联表';
COMMENT ON COLUMN playlist_song.id IS 'ID';
COMMENT ON COLUMN playlist_song.playlist_id IS '歌单 ID';
COMMENT ON COLUMN playlist_song.song_id IS '歌曲 ID';
COMMENT ON COLUMN playlist_song.added_by IS '添加人 ID（冗余，用于权限追溯）';
COMMENT ON COLUMN playlist_song.create_time IS '创建时间';

CREATE INDEX idx_playlist_song_playlist ON playlist_song(playlist_id);
CREATE INDEX idx_playlist_song_song ON playlist_song(song_id);

-- 外键约束（在Playlist领域DDL中定义）
ALTER TABLE playlist_song ADD CONSTRAINT fk_playlist_song_playlist
    FOREIGN KEY (playlist_id) REFERENCES playlist(id) ON DELETE CASCADE;
ALTER TABLE playlist_song ADD CONSTRAINT fk_playlist_song_song
    FOREIGN KEY (song_id) REFERENCES song(id) ON DELETE CASCADE;
```

## 8. 领域关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        音乐内容领域 (Song)                        │
│                                                                 │
│   ┌──────────────────────────────────────────────────────┐     │
│   │                   Song (聚合根)                       │     │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │     │
│   │  │    name     │  │   singer    │  │    album     │  │     │
│   │  └─────────────┘  └─────────────┘  └──────────────┘  │     │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │     │
│   │  │   status    │  │  paidLevel  │  │ totalClicks  │  │     │
│   │  └─────────────┘  └─────────────┘  └──────────────┘  │     │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │     │
│   │  │   version   │  │  createdBy  │  │   rule       │  │     │
│   │  └─────────────┘  └─────────────┘  └──────────────┘  │     │
│   └──────────────────────────────────────────────────────┘     │
│                              │                                  │
│           ┌──────────────────┼──────────────────┐              │
│           ▼                  ▼                  ▼              │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│   │  SongLabel   │  │SongClickLog  │  │SongClickStat │        │
│   │   (实体)      │  │   (实体)      │  │   (实体)      │        │
│   └──────────────┘  └──────────────┘  └──────────────┘        │
│                              │                                  │
└──────────────────────────────┼──────────────────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Auth 领域      │  │ Playlist 领域    │  │   Label 领域     │
│                 │  │                 │  │                 │
│  获取当前用户ID   │  │  歌曲关联到歌单   │  │  获取/创建标签   │
│  权限校验        │  │  查询歌单歌曲    │  │  建立歌曲标签关联 │
└─────────────────┘  └─────────────────┘  └─────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        领域事件流转                                  │
│                                                                   │
│  SongDeletedEvent ──────────┐                                      │
│                             ▼                                      │
│                    Playlist领域：解除歌单关联、刷新缓存                 │
│                                                                   │
│  LabelDeletedEvent ◀────────┘                                      │
│                             ▼                                      │
│                    Song领域：清理song_label关联                     │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

## 9. 验收标准

### 9.1 基础功能

1. 可以正常添加歌曲，必填项（歌曲名）校验生效
2. 添加歌曲支持选择目标歌单
3. 添加歌曲支持标签选择（可多选）
4. 语言支持预设选择（中/英/日/韩）或自由输入
5. 歌单列表正确展示所有添加的歌曲信息
6. 主播登录后，列表项显示勾选框，悬停显示编辑/删除按钮

### 9.2 编辑与删除

7. 编辑歌曲功能正常，可修改所有字段（包括状态、付费等级、点歌规则）
8. 编辑时校验版本号，冲突时提示"数据已被修改，请刷新后重试"
9. 删除歌曲需二次确认，确认后软删除成功
10. 批量删除功能正常，勾选多首歌曲后可批量删除
11. 批量修改状态功能正常，勾选多首歌曲后可批量修改状态
12. 删除歌曲时，关联的标签关联同步清理

### 9.3 点击统计

13. 用户点击歌曲跳转链接时，后端正确记录点击日志
14. 点击统计表更新：`total_clicks`、`today_clicks` 正确更新
15. 歌曲表冗余字段 `total_clicks` 同步更新
16. 支持获取歌曲点击统计数据

### 9.4 搜索与筛选

17. 搜索功能手动触发，可搜索歌曲名/歌手/专辑
18. 语言筛选功能正常，多选取并集
19. 标签筛选功能正常，多选取并集
20. 筛选条件组合正确（不同条件取交集）

### 9.5 排序

21. 排序支持按添加时间升序/降序
22. 排序支持按歌曲名A-Z排序
23. 排序支持按点击数（热门优先）排序

### 9.6 数据一致性

24. 歌曲数据独立存储，不同主播的相同歌曲互不影响
25. 歌曲状态支持手动修改：可点/学习中/暂不可点
26. 付费等级hover时展示文字说明
27. 点歌规则正确展示和编辑

### 9.7 边界条件校验

28. 非歌曲创建者无法编辑/删除歌曲（返回403）
29. 删除歌曲后，`playlist_song` 关联记录同步清理
30. 删除歌曲后，`song_label` 关联记录同步清理
31. 删除歌曲后，`SongDeletedEvent` 事件成功发布
32. 标签被删除后，`LabelDeletedEvent` 事件消费成功，清理关联
33. 所有查询附带 `deleted = 0` 过滤条件
34. 点击统计并发更新无数据丢失
35. 统计计数器按日/周/月正确重置

## 10. 领域不变量与边界条件（设计约束）

### 10.1 强制约束

1. **歌曲所有权不可转让**：`created_by` 创建后不可更改
2. **删除必清理**：删除歌曲时，必须在同一事务内清理 `song_label` 和 `playlist_song`
3. **事件必发布**：删除歌曲必须发布 `SongDeletedEvent`，无论是否同步清理
4. **状态机流转**：`status` 状态变更需符合业务规则（如从 `REMOVED` 恢复到 `AVAILABLE` 需审批？）
5. **统计原子性**：点击统计更新必须使用原子操作或乐观锁

### 10.2 业务规则

| 规则编号 | 规则描述 | 触发点 | 处理方式 |
|---------|---------|-------|---------|
| R-001 | 歌曲名必填 | 添加/编辑 | 校验失败返回400 |
| R-002 | 仅创建者可编辑 | 编辑/删除 | 校验 `createdBy == currentUserId` |
| R-003 | 歌单权限校验 | 添加歌曲 | 调用 `PlaylistService.validateOwnership()` |
| R-004 | 标签存在性校验 | 添加/编辑 | 调用 `LabelService.listAvailableLabels()` |
| R-005 | 软删除查询过滤 | 所有查询 | 自动附加 `deleted = 0` |
| R-006 | 点击统计并发安全 | 点击记录 | 使用 `UPDATE ... SET clicks = clicks + 1` 原子操作 |
| R-007 | 事件幂等处理 | 事件消费 | 基于业务键（如 `songId`、`labelId`）幂等 |

### 10.3 边界清晰度检查

✅ **数据所有权**：`song`、`song_label`、`song_click_stat`、`song_click_log` 归属 Song 领域  
✅ **外键约束**：所有外键关系明确，引用完整性有保障  
✅ **领域事件**：发布与消费边界清晰，幂等性有保障  
✅ **服务调用**：依赖的领域服务（Label、Playlist、Auth）已明确  
✅ **权限校验**：所有权校验、状态可见性规则明确  
✅ **软删除策略**：同步清理 + 异步通知的策略合理  
✅ **乐观锁**：`version` 字段使用场景明确  
✅ **关联维护**：`song_label` 和 `playlist_song` 的维护责任归属清晰  
✅ **点击统计**：统计表与日志表的职责分离  
✅ **搜索筛选**：跨领域协作（标签）方式明确  

---

**文档版本**：v2.0  
**最后更新**：2025-04-25  
**维护者**：Song领域团队
