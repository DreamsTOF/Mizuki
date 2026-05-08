# Skills 模块 API 设计文档

## 1. 接口列表

| 接口 | 说明 |
|------|------|
| 获取技能列表 | 获取所有技能，支持按分类和等级筛选 |
| 获取单个技能详情 | 根据技能 ID 获取完整技能信息 |
| 创建技能 | 新增一个技能记录 |
| 更新技能 | 根据技能 ID 更新技能信息 |
| 删除技能 | 根据技能 ID 删除技能记录 |
| 获取技能分类列表 | 获取所有分类及每个分类下的技能数量 |

## 2. 接口详情

### 2.1 获取技能列表

**功能描述**：获取技能列表，支持按分类和等级筛选。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 按分类筛选，可选值：frontend、backend、database、tools、other |
| level | string | 否 | 按等级筛选，可选值：beginner、intermediate、advanced、expert |

**响应数据**：

返回 Skill 对象数组，每个对象包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 技能唯一标识 |
| name | string | 技能名称 |
| description | string | 技能描述 |
| icon | string | Iconify 图标名称 |
| category | string | 技能分类 |
| level | string | 技能等级 |
| experience | object | 经验时长，包含 years 和 months |
| color | string | 技能主题色（可选） |
| projects | string[] | 关联项目 ID 列表（可选） |
| certifications | string[] | 认证证书列表（可选） |

### 2.2 获取单个技能详情

**功能描述**：根据技能 ID 获取单个技能的完整信息。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 技能唯一标识 |

**响应数据**：

返回单个 Skill 对象，字段与获取技能列表一致。

### 2.3 创建技能

**功能描述**：创建一个新的技能记录。

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 技能唯一标识，如 "javascript" |
| name | string | 是 | 技能名称，如 "JavaScript" |
| description | string | 是 | 技能描述文本 |
| icon | string | 是 | Iconify 图标名称 |
| category | string | 是 | 分类枚举值 |
| level | string | 是 | 等级枚举值 |
| experience.years | number | 是 | 经验年数，非负整数 |
| experience.months | number | 是 | 经验月数，0-11 |
| color | string | 否 | HEX 颜色值 |
| projects | string[] | 否 | 关联项目 ID 数组 |
| certifications | string[] | 否 | 证书名称数组 |

**响应数据**：

返回创建成功的 Skill 对象，包含完整的技能信息。

### 2.4 更新技能

**功能描述**：根据技能 ID 更新技能信息，只更新提供的字段。

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 技能唯一标识，用于定位 |
| name | string | 否 | 技能名称 |
| description | string | 否 | 技能描述文本 |
| icon | string | 否 | Iconify 图标名称 |
| category | string | 否 | 分类枚举值 |
| level | string | 否 | 等级枚举值 |
| experience.years | number | 否 | 经验年数 |
| experience.months | number | 否 | 经验月数 |
| color | string | 否 | HEX 颜色值 |
| projects | string[] | 否 | 关联项目 ID 数组 |
| certifications | string[] | 否 | 证书名称数组 |

**响应数据**：

返回更新后的 Skill 对象，包含完整的技能信息。

### 2.5 删除技能

**功能描述**：根据技能 ID 删除技能记录。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 技能唯一标识 |

**响应数据**：

返回删除操作的结果确认。

### 2.6 获取技能分类列表

**功能描述**：获取所有技能分类及每个分类下的技能数量，用于前端筛选标签展示。

**请求参数**：无

**响应数据**：

返回 SkillCategory 对象数组，每个对象包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| category | string | 分类标识，如 "frontend" |
| count | number | 该分类下的技能数量 |

此外，前端需要聚合所有分类的总数，用于展示 "全部" 标签。

## 3. 数据类型定义

### Skill

```typescript
interface Skill {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: "frontend" | "backend" | "database" | "tools" | "other";
  level: "beginner" | "intermediate" | "advanced" | "expert";
  experience: {
    years: number;
    months: number;
  };
  color?: string;
  projects?: string[];
  certifications?: string[];
}
```

### SkillCategory

```typescript
interface SkillCategory {
  category: string;
  count: number;
}
```

## 4. 校验规则

- id：唯一，只能包含字母、数字、连字符、下划线。
- name：非空字符串，长度建议不超过 50 个字符。
- description：非空字符串，长度建议不超过 500 个字符。
- icon：非空字符串，符合 Iconify 图标命名格式。
- category：必须是预定义的枚举值之一。
- level：必须是预定义的枚举值之一。
- experience.years：非负整数。
- experience.months：整数，取值范围 0-11。
- color：若提供，必须是有效的 HEX 颜色值格式。
