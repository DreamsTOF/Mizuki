# 身份认证领域（Auth Domain）需求文档

## 1. 领域概览

| 项目 | 内容 |
|------|------|
| 领域名称 | 身份认证（Auth） |
| 聚合根 | User |
| 核心职责 | 用户基础信息管理（注册、登录、slug生成） |
| 处理表 | `user` 表 |
| 领域角色 | 领域事件的生产者 |
| 接口路径前缀 | `/auth` |

## 2. 领域边界定义

### 2.1 边界内（核心职责）

本领域仅负责以下核心功能：

- **用户账号生命周期管理**：注册、登录、登出
- **用户基础信息管理**：用户名、密码、slug（URL标识）
- **身份认证与授权**：JWT Token 生成与验证、登录态管理

### 2.2 边界外（与其他领域的协作）

| 协作领域 | 协作关系 | 说明 |
|---------|---------|------|
| **Playlist（歌单管理）** | 发布领域事件 | 用户注册成功后发布 `UserRegisteredEvent`，Playlist领域监听并创建默认歌单 |
| **Song（音乐内容）** | 提供用户上下文 | 通过SaToken上下文提供当前登录用户ID，用于歌曲创建者标识 |
| **Label（标签管理）** | 提供用户上下文 | 通过SaToken上下文提供当前登录用户ID，用于标签创建者标识 |

### 2.3 关键边界规则

1. **Auth模块只处理user表，不处理歌单创建**
2. **用户注册成功后发布 `UserRegisteredEvent` 领域事件**
3. **歌单创建由Playlist模块监听事件后处理，不是Auth模块处理**

### 2.4 数据所有权边界

**表归属声明**：
- Auth领域仅维护 `user` 一张数据表
- `user` 表是Auth领域的唯一数据源，其他领域不得直接修改

**外键约束策略**：
- Auth领域不维护任何外键约束（Auth是源头领域）
- 其他领域通过业务逻辑关联用户ID，而非数据库外键

**与其他表的关联关系**：
| 关联表 | 字段 | 关系类型 | 说明 |
|--------|------|---------|------|
| playlist | user_id | 引用 | 创建者标识，允许用户删除后保留历史歌单 |
| song | created_by | 引用 | 创建者标识，允许用户删除后保留历史歌曲 |
| label | created_by | 引用 | 创建者标识，允许用户删除后保留历史标签 |

### 2.5 领域事件边界

**发布事件**：
- `UserRegisteredEvent`：用户注册成功后发布
  - 事件内容：`userId`, `username`, `slug`
  - 触发时机：用户持久化成功后
  - 事件消费者：Playlist领域

**消费事件**：
- Auth领域不监听任何其他领域的事件（单向依赖）

**事件契约**：
```java
public class UserRegisteredEvent implements DomainEvent {
    private final UUID userId;      // 用户ID（必填）
    private final String username;  // 用户名（必填）
    private final String slug;      // URL标识（必填）
    private final LocalDateTime occurredOn; // 事件发生时间
    // 注意：不包含密码等敏感信息
}
```

### 2.6 服务调用边界

**Auth领域提供的服务**：

| 服务接口 | 方法签名 | 说明 | 调用方 |
|---------|---------|------|--------|
| `UserService` | `UserDTO getById(UUID userId)` | 根据ID查询用户信息 | 所有领域 |
| `UserService` | `boolean existsByUsername(String username)` | 校验用户名是否存在 | 所有领域 |
| `AuthService` | `String authenticate(String username, String password)` | 用户认证，返回JWT Token | 用户接口层 |

**被其他领域调用的场景**：
- 获取当前登录用户ID（通过SaToken上下文）
- 校验用户是否存在（注册/创建资源时）
- 获取用户基本信息（展示创建者信息）

**禁止行为**：
- 禁止其他领域直接调用User实体的业务方法（如修改密码）
- 禁止其他领域直接持久化User实体
- 禁止跨领域事务（通过领域事件解耦）

### 2.7 权限校验边界

**用户权限规则**：
1. **用户自我管理**：
   - 用户只能修改自己的信息（昵称、头像、简介等）
   - 用户不能修改自己的用户名、slug、ID
   - 用户不能修改其他用户的信息

2. **管理员权限**：
   - 管理员可以管理所有用户
   - 管理员可启用/禁用用户账号
   - 管理员可查询所有用户（包括已删除用户）

3. **认证授权**：
   - 所有需要登录的接口必须校验JWT Token
   - 资源操作必须校验资源所有者与当前用户一致性

**权限校验点**：
- 修改用户信息前：校验 `currentUserId.equals(targetUserId)`
- 管理员操作前：校验用户角色为 `ADMIN`
- 删除用户前：校验用户状态（已删除用户禁止再次删除）

### 2.8 软删除边界

**软删除策略**：
- `user` 表使用 `deleted` 字段标记删除状态（0-未删除，1-已删除）
- 删除操作仅标记，不物理删除数据（保留历史记录）
- 已删除用户无法登录，但历史关联数据保留

**关联数据处理规则**：

| 关联表 | 处理策略 | 说明 |
|--------|---------|------|
| playlist | **保留** | 歌单历史数据不删除，creator_id保留原用户ID |
| song | **保留** | 歌曲历史数据不删除，created_by保留原用户ID |
| label | **保留** | 标签历史数据不删除，created_by保留原用户ID |

**查询过滤**：
- 常规查询自动过滤 `deleted = 1` 的用户
- 管理员查询可包含已删除用户（通过特殊接口）
- 领域事件不发布用户删除事件（保持简单）

### 2.9 并发控制边界

**乐观锁策略**：
- User聚合根使用 `version` 字段实现乐观锁
- 每次更新操作递增 `version`（基于JPA/Hibernate自动管理）
- 并发修改冲突时抛出 `OptimisticLockingFailureException`

**并发场景示例**：
- 用户同时修改昵称：后提交的请求失败，提示"数据已被修改，请刷新重试"
- 管理员同时禁用多个用户：每个用户独立版本号，互不影响

### 2.10 密码安全边界

**加密存储**：
- 密码必须使用 Argon2 或 bcrypt 算法加密
- 禁止明文存储、禁止使用MD5/SHA系列弱加密
- 密码加密参数（盐值、迭代次数）需符合安全标准

**登录安全**：
- 登录失败次数限制：同一用户连续失败5次，锁定30分钟（可选功能）
- JWT Token有效期：7天（可配置）
- Token刷新机制：支持无感刷新

**密码策略**：
- 密码长度：6-20位
- 密码复杂度：可选要求（字母+数字组合）
- 密码修改：需验证原密码

### 2.11 ID生成策略

**全局唯一ID**：
- 使用 UUIDv7 作为用户ID（时间排序、高并发友好）
- 生成方式：数据库函数 `uuidv7()` 或应用层生成
- ID格式：标准UUID字符串（36字符）

**业务ID（slug）**：
- 用户自定义URL标识，3-30位
- 格式：字母/数字/下划线/连字符
- 唯一性约束：数据库唯一索引（过滤软删除）
- 生成规则：用户名转换（小写、替换特殊字符）

### 2.12 与其他领域的数据流转

**Auth → Playlist**：
```
1. Auth发布 UserRegisteredEvent
2. Playlist监听事件
3. Playlist根据userId创建默认歌单
4. 数据流：UserRegisteredEvent.userId → PlaylistService.createDefaultForUser()
```

**Auth → Song**：
```
1. 用户上传歌曲时携带JWT Token
2. AuthService从Token解析userId
3. SongService使用userId作为created_by字段
4. 数据流：JWT Token → userId → Song.created_by
```

**Auth → Label**：
```
1. 用户创建标签时携带JWT Token
2. AuthService从Token解析userId
3. LabelService使用userId作为created_by字段
4. 数据流：JWT Token → userId → Label.created_by
```

**数据流转约束**：
- 仅通过ID传递用户标识，不传递完整用户对象
- 禁止跨领域直接访问数据库表
- 用户删除后，历史数据的created_by字段保留原值（可展示为"已注销用户"）

## 3. 核心领域模型

### 3.1 聚合根：User（用户）

```
User (聚合根)
├── id: UUID                    # 用户唯一标识
├── username: String            # 用户名（唯一，字母/数字/下划线，3-20位）
├── password: String            # 加密密码（Argon2/bcrypt）
├── nikeName: String            # 昵称
├── avatarUrl: String           # 头像URL
├── bio: String                 # 个人简介
├── personalSpaceUrl: String    # 个人空间链接
├── liveRoomUrl: String         # 直播间链接
├── lastLoginTime: DateTime     # 最后登录时间
├── lastLoginIp: String         # 最后登录IP
├── createTime: DateTime        # 创建时间
├── updateTime: DateTime        # 更新时间
├── deleted: Integer            # 删除标记：0-未删除，1-已删除
└── version: Integer            # 版本号，用于乐观锁
```

### 3.2 值对象

| 值对象 | 说明 | 校验规则 |
|-------|------|---------|
| Username | 用户名 | 字母/数字/下划线，3-20位，唯一 |
| Slug | URL标识 | 字母/数字/下划线/连字符，3-30位，唯一 |
| Password | 密码 | 6-20位，加密存储 |
| AvatarUrl | 头像 | URL格式，可选 |

### 3.3 枚举

```java
public enum UserStatus {
    ENABLED,    // 启用
    DISABLED    // 禁用
}
```

## 4. 功能需求

### 4.1 用户注册

- **功能描述**：新用户（主播）注册账号
- **前置条件**：用户未登录状态
- **主流程**：
  1. 用户输入用户名、密码、确认密码、slug
  2. 系统校验用户名格式、slug格式、密码长度、两次密码一致性
  3. 校验通过后创建用户账号，自动登录并跳转到歌单页
  4. 发布 `UserRegisteredEvent` 领域事件
- **异常流程**：
  - 用户名已存在：提示"该用户名已被使用，请更换"
  - slug已存在：提示"该URL标识已被使用，请更换"
  - 密码不符合要求：提示"密码长度需在6-20位之间"
  - 两次密码不一致：提示"两次输入的密码不一致"

### 4.2 用户登录

- **功能描述**：已有账号用户登录系统
- **前置条件**：用户未登录状态
- **主流程**：
  1. 用户输入用户名和密码
  2. 系统校验账号密码正确性
  3. 校验通过后生成JWT Token，登录成功，跳转到歌单页
- **异常流程**：
  - 用户不存在：提示"用户不存在，请先注册"
  - 密码错误：提示"密码错误"

### 4.3 用户登出

- **功能描述**：退出当前登录账号
- **前置条件**：用户已登录
- **主流程**：
  1. 用户点击登出按钮
  2. 系统清除登录态（JWT失效），跳转到登录页或首页

## 5. 领域事件定义

### 5.1 UserRegisteredEvent（用户注册成功事件）

| 属性 | 类型 | 说明 |
|------|------|------|
| userId | UUID | 新注册用户的唯一标识 |
| username | String | 用户名 |
| nikeName | String | 昵称（可选） |
| slug | String | URL标识 |
| occurredOn | DateTime | 事件发生时间 |

**触发时机**：用户注册成功并持久化后

**消费者**：Playlist领域监听此事件，为用户创建默认歌单

```java
// 领域事件定义示例
public class UserRegisteredEvent implements DomainEvent {
    private final UUID userId;
    private final String username;
    private final String nikeName;
    private final String slug;
    private final LocalDateTime occurredOn;
    
    // constructor, getters...
}
```

## 6. 接口清单

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 用户注册 | POST | `/auth/register` | 创建用户账号，发布领域事件 |
| 用户登录 | POST | `/auth/login` | 用户登录，返回JWT |
| 用户登出 | POST | `/auth/logout` | 退出登录 |
| 获取当前用户 | GET | `/auth/me` | 获取当前登录用户信息 |

## 7. 数据库表结构

### 7.1 user 表

```sql
CREATE TABLE "user" (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nike_name VARCHAR(50) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    bio TEXT DEFAULT NULL,
    personal_space_url VARCHAR(255) DEFAULT NULL,
    live_room_url VARCHAR(255) DEFAULT NULL,
    last_login_time TIMESTAMP DEFAULT NULL,
    last_login_ip VARCHAR(50) DEFAULT NULL,
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0,
    version INT DEFAULT 0
);

COMMENT ON TABLE "user" IS '用户信息表';
COMMENT ON COLUMN "user".id IS 'ID';
COMMENT ON COLUMN "user".username IS '用户名（唯一标识）';
COMMENT ON COLUMN "user".password IS '密码';
COMMENT ON COLUMN "user".nike_name IS '昵称';
COMMENT ON COLUMN "user".avatar_url IS '头像URL';
COMMENT ON COLUMN "user".bio IS '个人简介';
COMMENT ON COLUMN "user".personal_space_url IS '个人空间链接';
COMMENT ON COLUMN "user".live_room_url IS '直播间链接';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".last_login_ip IS '最后登录IP';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".update_time IS '更新时间';
COMMENT ON COLUMN "user".deleted IS '删除标记：0-未删除，1-已删除';
COMMENT ON COLUMN "user".version IS '版本号，用于乐观锁';

CREATE UNIQUE INDEX uk_username ON "user"(username) WHERE deleted = 0;
CREATE INDEX idx_user_create_time ON "user"(create_time);
```

## 8. 领域关系图

```
┌─────────────────────────────────────────────────────────────────────┐
│                       身份认证领域 (Auth Domain)                      │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                  User (聚合根)                               │   │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐        │   │
│   │  │  id(UUIDv7) │  │  username   │  │  password    │        │   │
│   │  └─────────────┘  └─────────────┘  └──────────────┘        │   │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐        │   │
│   │  │    slug     │  │  deleted    │  │  version     │        │   │
│   │  └─────────────┘  └─────────────┘  └──────────────┘        │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                              │                                        │
│                              │ 发布领域事件                            │
│                              ▼                                        │
│                  ┌─────────────────────┐                             │
│                  │ UserRegisteredEvent │                             │
│                  │  ┌───────────────┐  │                             │
│                  │  │ userId        │  │                             │
│                  │  │ username      │  │                             │
│                  │  │ slug          │  │                             │
│                  │  └───────────────┘  │                             │
│                  └─────────────────────┘                             │
│                              │                                        │
│                              │ 监听                                  │
└──────────────────────────────┼───────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────┼───────────────────────────────────────┐
│                      歌单管理领域 (Playlist Domain)                    │
│                              │                                        │
│                    ┌─────────────────────┐                            │
│                    │ 创建默认歌单          │                            │
│                    │ createDefaultForUser│                            │
│                    └─────────────────────┘                            │
└───────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────┼───────────────────────────────────────┐
│                      音乐内容领域 (Song Domain)                        │
│                              │                                        │
│                    ┌─────────────────────┐                            │
│                    │ 使用userId作为        │                            │
│                    │ created_by字段       │                            │
│                    └─────────────────────┘                            │
└───────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────┼───────────────────────────────────────┐
│                      标签管理领域 (Label Domain)                       │
│                              │                                        │
│                    ┌─────────────────────┐                            │
│                    │ 使用userId作为        │                            │
│                    │ created_by字段       │                            │
│                    └─────────────────────┘                            │
└───────────────────────────────────────────────────────────────────────┘

边界说明：
────────────────────────────────────────────────────────────────────────
数据流向：
  Auth → Playlist : 单向事件驱动（UserRegisteredEvent）
  Auth → Song     : 单向ID引用（JWT Token → userId）
  Auth → Label    : 单向ID引用（JWT Token → userId）

所有权：
  ✓ Auth领域拥有user表的完全所有权
  ✗ 其他领域不得修改user表

软删除影响：
  user.deleted = 1
    ├─> 用户无法登录
    ├─> playlist保留（显示为"已注销用户"）
    ├─> song保留（显示为"已注销用户"）
    └─> label保留（显示为"已注销用户"）

并发控制：
  user.version 乐观锁保护
    ├─> 用户信息修改冲突检测
    └─> 管理员操作冲突检测
```

## 9. 领域协作时序图

```
用户注册流程：

用户                          Auth领域                    Playlist领域
 │                               │                              │
 │── 1. 提交注册信息 ───────────>│                              │
 │                               │                              │
 │                               │── 2. 校验并创建User聚合根     │
 │                               │                              │
 │                               │── 3. 持久化到user表           │
 │                               │                              │
 │                               │── 4. 发布UserRegisteredEvent  │
 │                               │─────────────────────────────>│
 │                               │                              │
 │                               │                              │── 5. 创建默认歌单
 │                               │                              │
 │<── 6. 返回注册成功 ───────────│                              │
 │                               │                              │
 │                               │                              │── 7. 歌单创建完成
```

## 10. 验收标准

### 10.1 核心功能

1. **用户可以正常注册、登录、登出**
   - 注册时校验用户名唯一性（字母/数字/下划线，3-20位）
   - 注册时校验slug唯一性（字母/数字/下划线/连字符，3-30位）
   - 密码长度校验（6-20位）
   - 两次密码一致性校验
   - 登录成功后返回JWT Token，有效期7天

2. **登录态管理**
   - 登录态保持正常
   - 退出后清除登录态
   - 未登录用户无法访问需要认证的接口

3. **领域事件发布**
   - 用户注册成功后正确发布 `UserRegisteredEvent`
   - 事件包含 userId、username、slug 等必要信息
   - 不包含密码等敏感信息

### 10.2 数据所有权边界

4. **表归属与维护**
   - Auth领域仅维护 `user` 表，不得操作其他表
   - 其他领域通过业务逻辑关联用户ID，而非数据库外键
   - `playlist.user_id`、`song.created_by`、`label.created_by` 引用 `user.id`

### 10.3 领域事件边界

5. **事件发布与消费**
   - 仅发布 `UserRegisteredEvent` 事件
   - 事件触发时机：用户持久化成功后
   - 事件消费者：仅Playlist领域
   - Auth领域不监听任何其他领域事件

### 10.4 服务调用边界

6. **服务提供与调用**
   - 提供 `UserService.getById()` 查询用户信息
   - 提供 `UserService.existsByUsername()` 校验用户名
   - 提供 `AuthService.authenticate()` 进行认证
   - 禁止其他领域直接调用User实体业务方法
   - 禁止跨领域事务

### 10.5 权限校验边界

7. **用户权限**
   - 用户只能修改自己的信息（昵称、头像、简介）
   - 用户不能修改用户名、slug、ID
   - 用户不能操作其他用户的数据

8. **管理员权限**
   - 管理员可管理所有用户
   - 管理员可启用/禁用用户账号
   - 管理员可查询所有用户（包括已删除）

### 10.6 软删除边界

9. **软删除策略**
   - 使用 `deleted` 字段标记删除状态
   - 删除操作仅标记，不物理删除
   - 已删除用户无法登录

10. **关联数据保留**
    - playlist保留，creator_id保留原值
    - song保留，created_by保留原值
    - label保留，created_by保留原值
    - 前端展示已删除用户时显示"已注销用户"

### 10.7 并发控制边界

11. **乐观锁**
    - User聚合根使用 `version` 字段
    - 并发修改冲突时提示"数据已被修改，请刷新重试"
    - 管理员批量操作互不影响（独立版本号）

### 10.8 密码安全边界

12. **密码加密**
    - 使用 Argon2 或 bcrypt 算法加密
    - 禁止明文存储、禁止弱加密算法

13. **登录安全**
    - JWT Token有效期7天（可配置）
    - 支持Token无感刷新
    - 登录失败5次锁定30分钟（可选）

### 10.9 ID生成边界

14. **ID生成**
    - 用户ID使用 UUIDv7 生成
    - slug使用用户名转换（小写、替换特殊字符）
    - slug在数据库具有唯一索引（过滤软删除）

### 10.10 数据流转边界

15. **跨领域数据传递**
    - Auth → Playlist：通过 `UserRegisteredEvent` 传递userId
    - Auth → Song：通过JWT Token解析userId
    - Auth → Label：通过JWT Token解析userId
    - 仅传递ID，不传递完整用户对象

16. **边界约束**
    - Auth模块只处理user表，不直接操作playlist表
    - 歌单创建由Playlist领域通过监听领域事件后处理
    - 用户删除后，历史数据的created_by字段保留原值
