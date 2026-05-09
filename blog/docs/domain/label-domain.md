# 标签管理领域（Label Domain）需求文档

## 1. 领域概览

| 项目 | 内容 |
|------|------|
| 领域名称 | 标签管理（Label） |
| 模块名 | labels |
| 聚合根 | Label |
| 核心职责 | 标签系统（预设+自定义）、颜色管理 |
| 处理表 | label |
| 角色 | 被Song领域依赖 |

## 2. 领域边界定义

### 2.1 边界内（核心职责）

本领域负责所有与标签定义相关的功能，**仅处理label表**：

- **标签生命周期管理**：创建（系统预设+用户自定义）、查询
- **标签分类管理**：区分系统预设标签（is_system=true）和用户自定义标签（is_system=false）
- **标签样式管理**：标签颜色自定义（默认天青色 #00BFFF）
- **标签可见性控制**：系统预设标签所有用户可见，用户自定义标签仅创建者可见

### 2.2 边界外（与其他领域的协作）

| 协作领域 | 协作关系 | 说明 |
|---------|---------|------|
| **Auth（身份认证）** | 依赖 | 获取当前登录用户ID用于自定义标签的创建者标识和权限校验 |
| **Song（音乐内容）** | 被依赖 | Song领域在添加/编辑歌曲时调用Label领域服务获取标签列表或创建自定义标签 |

**重要边界约定**：
- **song_label关联表不在Label领域维护**，由Song领域负责管理
- Label领域仅提供标签查询服务供Song领域使用
- 标签与歌曲的关联关系由Song模块维护

### 2.3 领域事件

| 事件名称 | 类型 | 触发时机 | 消费者 | 说明 |
|---------|------|---------|--------|------|
| `LabelCreatedEvent` | 发布 | 标签创建成功 | 可选 | 新标签创建事件 |
| `LabelDeletedEvent` | **发布** | **标签删除成功** | **Song领域** | **标签删除事件，Song领域监听并清理关联** |

## 3. 核心领域模型

### 3.1 聚合根：Label（标签）

```
Label (聚合根)
├── id: UUID                    # 标签唯一标识
├── name: String                # 标签名称
├── color: String               # 标签颜色（HEX格式，默认#00BFFF）
├── isSystem: Boolean           # 是否系统预设标签
├── createdBy: UUID             # 创建者用户ID（系统预设为null）
├── version: Long               # 乐观锁版本号
├── createdAt: DateTime         # 创建时间
├── updatedAt: DateTime         # 更新时间
└── deleted: Int                # 删除标记：0-未删除，1-已删除
```

### 3.2 值对象

| 值对象 | 说明 | 校验规则 |
|-------|------|---------|
| LabelName | 标签名称 | 1-50字符，同一用户下唯一（系统预设标签全局唯一） |
| LabelColor | 标签颜色 | HEX格式，默认#00BFFF |

### 3.3 枚举

```java
public enum LabelType {
    SYSTEM,     // 系统预设 - 所有用户可见，仅管理员可创建
    CUSTOM      // 用户自定义 - 仅创建者可见
}
```

## 4. 功能需求

### 4.1 标签创建

- **功能描述**：创建新标签（系统预设或用户自定义）
- **前置条件**：
  - 系统预设标签：管理员权限
  - 用户自定义标签：主播已登录
- **主流程**：
  1. 用户输入标签名称
  2. 选择标签颜色（默认 #00BFFF 天青色）
  3. 系统校验标签名称唯一性：
     - 系统预设标签：全局唯一
     - 用户自定义标签：同一用户下唯一
  4. 创建标签记录：
     - 系统预设标签：is_system=true，created_by=null
     - 用户自定义标签：is_system=false，created_by=当前用户ID
- **业务规则**：
  - 系统预设标签：is_system=true，所有用户可见，仅管理员可创建
  - 用户自定义标签：is_system=false，仅创建者可见
  - 同一用户下标签名称唯一
  - 不同用户可创建同名标签
- **异常流程**：
  - 标签名称重复：提示"该标签已存在"

### 4.2 标签查询

- **功能描述**：查询标签列表
- **前置条件**：用户已登录或访问公开歌单
- **主流程**：
  1. 系统查询标签列表
  2. 返回系统预设标签（is_system=true）
  3. 如用户已登录，同时返回该用户的自定义标签（is_system=false, created_by=当前用户）
- **后置条件**：标签列表正确展示，用于歌曲添加和筛选

### 4.3 标签删除

- **功能描述**：删除自定义标签
- **前置条件**：
  - 标签为用户自定义标签（is_system=false）
  - 当前用户为标签创建者
- **主流程**：
  1. 校验标签存在且为用户自定义标签
  2. 校验当前用户为标签创建者
  3. 执行标签软删除（deleted=1）
  4. **发布 `LabelDeletedEvent` 领域事件**
- **业务规则**：
  - 系统预设标签不允许删除
  - 仅创建者可删除自己的自定义标签
- **协作说明**：
  - **标签删除后发布 `LabelDeletedEvent` 事件**
  - **Song领域监听该事件，负责清理song_label关联表中的相关记录**
  - Label领域不直接操作song_label表

## 5. 与其他领域的协作关系

### 5.1 与Song领域的双向协作

Label领域与Song领域存在双向协作关系：

| 协作场景 | 方向 | Label领域职责 | Song领域职责 |
|---------|------|--------------|-------------|
| 获取标签列表 | Label → Song | 提供查询服务，返回系统预设+当前用户自定义标签 | 调用Label领域服务获取标签数据 |
| 创建自定义标签 | Label → Song | 创建标签记录，返回标签ID | 在添加歌曲时，若标签不存在则调用创建 |
| 标签与歌曲关联 | Label → Song | **不涉及** | **负责维护song_label关联表** |
| **标签删除清理** | **Song → Label** | **发布`LabelDeletedEvent`事件** | **监听事件，清理song_label关联记录** |

### 5.2 领域协作时序图

#### 5.2.1 添加歌曲时使用标签流程

```
Song领域                    Label领域
   │                              │
   │  1. 查询可用标签列表          │
   │─────────────────────────────>│
   │                              │
   │<─────────────────────────────│
   │  2. 返回系统预设+用户自定义标签│
   │                              │
   │  3. 用户选择/输入标签         │
   │                              │
   │  4. 需要创建新标签？          │
   │─────────────────────────────>│
   │                              │
   │                              │  5. 创建自定义标签
   │                              │     (is_system=false)
   │                              │
   │<─────────────────────────────│
   │  6. 返回新标签ID             │
   │                              │
   │  7. 建立song_label关联       │
   │     （Song领域自行维护）      │
   │
```

#### 5.2.2 标签删除事件流程（新增）

```
Label领域                   Song领域
   │                              │
   │  1. 删除标签                  │
   │  执行软删除 deleted=1         │
   │                              │
   │  2. 发布LabelDeletedEvent    │
   │─────────────────────────────>│
   │  (labelId, deletedAt)        │
   │                              │
   │                              │  3. 监听事件
   │                              │
   │                              │  4. 删除song_label
   │                              │     关联记录
   │                              │
   │<─────────────────────────────│
   │  5. 清理完成                  │
```

## 6. 接口清单

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 创建标签 | POST | `/labels/save` | 创建自定义标签 |
| 更新标签 | PUT | `/labels/update` | 更新标签信息（仅自定义标签） |
| 删除标签 | DELETE | `/labels/remove/{id}` | 删除自定义标签（仅创建者可删） |
| 批量删除标签 | DELETE | `/labels/removeByIds` | 批量删除自定义标签 |
| 获取标签详情 | GET | `/labels/detail/{id}` | 获取标签详情 |
| 获取标签列表 | GET | `/labels/list` | 获取系统预设+当前用户自定义标签 |
| 分页查询标签 | POST | `/labels/page` | 分页查询标签（管理后台用） |
| 游标查询标签 | POST | `/labels/seek` | 游标查询标签 |
| 获取系统预设标签 | GET | `/labels/system` | 仅获取系统预设标签 |
| 获取我的标签 | GET | `/labels/mine` | 仅获取当前用户的自定义标签 |

## 7. 数据库表结构

### 7.1 label表

```sql
CREATE TABLE label (
    id UUID DEFAULT uuidv7() PRIMARY KEY,
    
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#00BFFF',
    is_system BOOLEAN DEFAULT FALSE,
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID DEFAULT NULL,
    deleted INT DEFAULT 0,
    version INT DEFAULT 0
);

COMMENT ON TABLE label IS '标签信息表';
COMMENT ON COLUMN label.id IS 'ID';
COMMENT ON COLUMN label.name IS '标签名称';
COMMENT ON COLUMN label.color IS '标签颜色，如：#00BFFF 天青色';
COMMENT ON COLUMN label.is_system IS '是否为系统预设：true-系统预设，false-用户自定义';
COMMENT ON COLUMN label.create_time IS '创建时间';
COMMENT ON COLUMN label.update_time IS '更新时间';
COMMENT ON COLUMN label.created_by IS '创建人 ID';
COMMENT ON COLUMN label.deleted IS '删除标记：0-未删除，1-已删除';
COMMENT ON COLUMN label.version IS '版本号，用于乐观锁';

CREATE INDEX idx_label_is_system ON label(is_system);
CREATE INDEX idx_label_name ON label(name);
CREATE INDEX idx_label_created_by ON label(created_by);
```

### 7.2 表关系说明

- **Label领域仅维护label表**
- **song_label关联表由Song领域维护**，位于song模块
- Label领域通过领域服务接口向Song领域提供标签查询能力

## 8. 系统预设标签列表

| 标签名称 | 颜色 | 类型 |
|---------|------|------|
| 流行 | #FF6B6B | 系统预设 |
| 摇滚 | #4ECDC4 | 系统预设 |
| 民谣 | #45B7D1 | 系统预设 |
| 古风 | #F7DC6F | 系统预设 |
| 电子 | #BB8FCE | 系统预设 |
| R&B | #85C1E9 | 系统预设 |
| 说唱 | #F8B500 | 系统预设 |
| 轻音乐 | #00BFFF | 系统预设 |

### 初始化SQL

```sql
INSERT INTO label (name, color, is_system, created_by) VALUES
('流行',  '#FF6B6B', TRUE, NULL),
('摇滚',  '#4ECDC4', TRUE, NULL),
('民谣',  '#45B7D1', TRUE, NULL),
('古风',  '#F7DC6F', TRUE, NULL),
('电子',  '#BB8FCE', TRUE, NULL),
('R&B', '#85C1E9', TRUE, NULL),
('说唱',  '#F8B500', TRUE, NULL),
('轻音乐','#00BFFF', TRUE, NULL);
```

## 9. 领域关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        标签管理领域 (Label)                       │
│                                                                 │
│   ┌──────────────────────────────────────────────────────┐     │
│   │                  Label (聚合根)                       │     │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │     │
│   │  │    name     │  │    color    │  │  isSystem    │  │     │
│   │  └─────────────┘  └─────────────┘  └──────────────┘  │     │
│   │  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  │     │
│   │  │  createdBy  │  │   version   │  │   deleted    │  │     │
│   │  └─────────────┘  └─────────────┘  └──────────────┘  │     │
│   └──────────────────────────────────────────────────────┘     │
│                              │                                  │
│                              │                                  │
│   ┌──────────────────────────┴──────────────────────────┐      │
│   │                      label表                         │      │
│   │              （Label领域唯一维护的表）                │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
└──────────────────────────────┬──────────────────────────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Auth 领域      │  │ Playlist 领域    │  │   Song 领域      │
│                 │  │                 │  │                 │
│  获取当前用户ID   │  │  无直接协作      │  │  依赖Label领域   │
│  权限校验        │  │                 │  │  获取/创建标签   │
│                 │  │                 │  │  维护song_label  │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## 10. 验收标准

1. **系统预设标签**：系统预设标签列表完整（流行、摇滚、民谣、古风、电子、R&B、说唱、轻音乐等）
2. **自定义标签**：主播可创建自定义标签，自定义标签仅自己可见（is_system=false, created_by=当前用户ID）
3. **标签名称唯一性**：
   - 系统预设标签名称全局唯一
   - 同一用户下自定义标签名称唯一
   - 不同用户可创建同名标签
4. **标签颜色**：标签支持自定义颜色，默认天青色（#00BFFF）
5. **乐观锁**：标签实体包含version字段，支持乐观锁
6. **领域边界**：Label领域仅操作label表，不直接操作song_label表
7. **服务提供**：Label领域提供标签查询服务供Song领域调用
8. **删除限制**：系统预设标签不允许删除，仅创建者可删除自定义标签
9. **接口规范**：遵循标准API路径规范（/labels/save, /labels/list等）
10. **数据类型**：使用UUID作为主键，符合项目统一规范
