# Skills 模块需求文档

## 1. 模块概述

Skills 模块用于管理博客系统中展示的个人技能信息，包括技能的增删改查以及分类筛选功能。

## 2. 功能需求

### 2.1 获取技能列表

**功能描述**：获取所有技能数据，支持按分类和等级进行筛选。

**需要的数据**：
- 技能唯一标识（id）
- 技能名称（name）
- 技能描述（description）
- 技能图标（icon）
- 技能分类（category）
- 技能等级（level）
- 使用经验时长（experience：年、月）
- 技能主题色（color）
- 可选：关联项目列表（projects）
- 可选：认证证书列表（certifications）

**筛选条件**：
- 按分类筛选：frontend、backend、database、tools、other
- 按等级筛选：beginner、intermediate、advanced、expert

### 2.2 获取单个技能详情

**功能描述**：根据技能唯一标识获取单个技能的完整信息。

**需要的数据**：
- 技能唯一标识（id）
- 技能名称（name）
- 技能描述（description）
- 技能图标（icon）
- 技能分类（category）
- 技能等级（level）
- 使用经验时长（experience：年、月）
- 技能主题色（color）
- 关联项目列表（projects）
- 认证证书列表（certifications）

### 2.3 创建技能

**功能描述**：创建一个新的技能记录。

**需要的数据**：
- 技能名称（name）：必填
- 技能描述（description）：必填
- 技能图标（icon）：必填，Iconify 图标名称
- 技能分类（category）：必填，枚举值：frontend、backend、database、tools、other
- 技能等级（level）：必填，枚举值：beginner、intermediate、advanced、expert
- 使用经验时长（experience）：必填，包含年（years）和月（months）
- 技能主题色（color）：可选，HEX 颜色值
- 关联项目列表（projects）：可选，项目 ID 数组
- 认证证书列表（certifications）：可选，证书名称数组

### 2.4 更新技能

**功能描述**：根据技能唯一标识更新技能信息。

**需要的数据**：
- 技能唯一标识（id）：用于定位要更新的技能
- 技能名称（name）：可选更新
- 技能描述（description）：可选更新
- 技能图标（icon）：可选更新
- 技能分类（category）：可选更新
- 技能等级（level）：可选更新
- 使用经验时长（experience）：可选更新
- 技能主题色（color）：可选更新
- 关联项目列表（projects）：可选更新
- 认证证书列表（certifications）：可选更新

### 2.5 删除技能

**功能描述**：根据技能唯一标识删除技能记录。

**需要的数据**：
- 技能唯一标识（id）：用于定位要删除的技能

### 2.6 获取所有技能分类

**功能描述**：获取系统中所有可用的技能分类列表，用于前端筛选标签展示。

**需要的数据**：
- 分类标识（category）：如 frontend、backend、database、tools、other
- 分类名称：用于前端展示的多语言文本键（由前端维护映射）
- 分类图标：用于前端展示的图标标识（由前端维护映射）
- 该分类下的技能数量

## 3. 数据模型

### Skill 技能

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 技能唯一标识，如 "javascript"、"react" |
| name | string | 是 | 技能名称，如 "JavaScript"、"React" |
| description | string | 是 | 技能描述文本 |
| icon | string | 是 | Iconify 图标名称，如 "logos:javascript" |
| category | enum | 是 | 分类：frontend、backend、database、tools、other |
| level | enum | 是 | 等级：beginner、intermediate、advanced、expert |
| experience.years | number | 是 | 经验年数 |
| experience.months | number | 是 | 经验月数 |
| color | string | 否 | 技能主题色，HEX 格式，如 "#F7DF1E" |
| projects | string[] | 否 | 关联项目 ID 列表 |
| certifications | string[] | 否 | 认证证书名称列表 |

### SkillCategory 技能分类

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 是 | 分类标识，如 "frontend" |
| count | number | 是 | 该分类下的技能数量 |

## 4. 枚举值定义

### 分类（Category）

| 值 | 说明 |
|----|------|
| frontend | 前端技术 |
| backend | 后端技术 |
| database | 数据库 |
| tools | 工具 |
| other | 其他 |

### 等级（Level）

| 值 | 说明 |
|----|------|
| beginner | 入门 |
| intermediate | 中级 |
| advanced | 高级 |
| expert | 专家 |

## 5. 业务规则

- 技能唯一标识（id）在系统内必须唯一。
- 创建技能时，name、description、icon、category、level、experience 为必填字段。
- experience 中 years 和 months 均为非负整数，months 取值范围为 0-11。
- color 字段为可选，若未提供，前端将使用默认颜色。
- 更新技能时，只允许更新指定的字段，未提供的字段保持原值不变。
- 删除技能时，需要确认该技能是否存在，不存在则操作失败。
