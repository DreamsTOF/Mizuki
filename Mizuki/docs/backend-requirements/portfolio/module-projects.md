# Projects（项目展示）模块需求文档

> **模块名称**: Projects
> **前端数据文件**: `src/data/projects.ts`
> **前端页面**: `src/pages/projects.astro`
> **前端组件**: `src/components/features/projects/ProjectCard.astro`, `src/components/features/featured-projects/FeaturedProjects.astro`
> **文档版本**: v2.0
> **最后更新**: 2026-05-04

---

## 1. 模块概述

Projects 模块是 Mizuki 博客系统中用于展示个人项目作品集的核心模块。它以卡片网格形式展示项目列表，支持按类别筛选、置顶项目高亮展示，并提供完整的管理后台功能，包括项目的增删改查。

### 1.1 核心目标

- 以视觉化卡片网格展示个人项目作品
- 支持按项目类别（web / mobile / desktop / other）筛选浏览
- 通过 `featured` 标记突出展示重点项目
- 展示项目元数据：技术栈、开发状态、时间线、外部链接等
- 提供管理后台，支持项目的增删改查

### 1.2 用户角色

| 角色 | 权限 |
|------|------|
| 访客 | 查看公开的项目列表、按类别筛选、访问项目链接 |
| 管理员 | 创建、编辑、删除项目，设置项目置顶状态 |

---

## 2. 数据结构

### 2.1 核心接口定义

文件路径：`src/data/projects.ts`

```typescript
export interface Project {
  id: string;                          // 唯一标识符（自定义字符串 ID）
  title: string;                       // 项目标题
  description: string;                 // 项目描述
  image: string;                       // 项目封面图片路径
  category: "web" | "mobile" | "desktop" | "other";  // 项目类别
  techStack: string[];                 // 技术栈标签列表
  status: "completed" | "in-progress" | "planned";   // 项目状态
  liveDemo?: string;                   // 在线演示地址（可选）
  sourceCode?: string;                 // 源码仓库地址（可选）
  visitUrl?: string;                   // 项目主页地址（可选）
  startDate: string;                   // 开始日期，格式 YYYY-MM-DD
  endDate?: string;                    // 结束日期，格式 YYYY-MM-DD（可选）
  featured?: boolean;                  // 是否置顶/精选展示
  tags?: string[];                     // 额外标签列表（可选）
  showImage?: boolean;                 // 是否显示封面图（可选，默认 true）
}
```

### 2.2 字段详细说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 全局唯一标识，使用自定义字符串（如 `"mizuki"`, `"folkpatch"`），非自增数字 |
| `title` | `string` | 是 | 项目标题，如 "Mizuki", "FolkPatch" |
| `description` | `string` | 是 | 项目简短描述，用于卡片展示 |
| `image` | `string` | 是 | 封面图片路径，如 `"/assets/projects/mizuki.webp"`；可为空字符串表示无图 |
| `category` | `enum` | 是 | 项目类别：`web`（网页应用）、`mobile`（移动应用）、`desktop`（桌面应用）、`other`（其它） |
| `techStack` | `string[]` | 是 | 技术栈数组，如 `["Astro", "TypeScript", "Tailwind CSS"]` |
| `status` | `enum` | 是 | 项目状态：`completed`（已完成）、`in-progress`（进行中）、`planned`（已计划） |
| `liveDemo` | `string` | 否 | 在线演示 URL |
| `sourceCode` | `string` | 否 | 源码仓库 URL（如 GitHub） |
| `visitUrl` | `string` | 否 | 项目主页/访问 URL |
| `startDate` | `string` | 是 | 项目开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 否 | 项目结束日期，格式 `YYYY-MM-DD` |
| `featured` | `boolean` | 否 | 是否为置顶/精选项目，默认 `false` |
| `tags` | `string[]` | 否 | 额外标签数组，如 `["Blog", "Theme", "Open Source"]` |
| `showImage` | `boolean` | 否 | 是否在前端显示封面图区域，默认 `true` |

### 2.3 数据示例

```typescript
{
  id: "mizuki",
  title: "Mizuki",
  description: "A next-gen Material Design 3 blog theme built with Astro...",
  image: "/assets/projects/mizuki.webp",
  category: "web",
  techStack: ["Astro", "TypeScript", "Tailwind CSS", "Svelte"],
  status: "completed",
  sourceCode: "https://github.com/matsuzaka-yuki/Mizuki",
  visitUrl: "https://mizuki.mysqil.com",
  startDate: "2024-01-01",
  endDate: "2024-06-01",
  featured: true,
  tags: ["Blog", "Theme", "Open Source"],
}
```

---

## 3. 功能需求

### 3.1 获取项目列表

**功能描述**：获取所有项目列表，支持筛选和排序。

**需要的数据**：
- 项目完整字段（id, title, description, image, category, techStack, status, liveDemo, sourceCode, visitUrl, startDate, endDate, featured, tags, showImage）
- 支持按 `category` 筛选（web / mobile / desktop / other）
- 支持按 `status` 筛选（completed / in-progress / planned）
- 支持按 `featured` 筛选（true / false）
- 支持按 `tech_stack` 筛选（精确匹配技术名称）
- 默认排序：`featured` 优先，然后按 `sort_order` 或创建时间排序
- 支持分页（limit / offset）

**前端用途**：
- `projects.astro` 页面渲染项目卡片网格
- `FilterTabs` 组件按类别统计项目数量
- 筛选交互后动态显示/隐藏项目卡片

---

### 3.2 获取单个项目详情

**功能描述**：根据项目 ID 获取单个项目的完整详情。

**需要的数据**：
- 项目完整字段（同 3.1）

**前端用途**：
- 项目详情页展示（如有独立详情页）
- 管理后台编辑时回显数据

---

### 3.3 创建项目

**功能描述**：创建新的项目记录。

**需要的数据**：
- `id`：自定义字符串 ID，全局唯一
- `title`：项目标题
- `description`：项目描述
- `image`：封面图片路径（可为空字符串）
- `category`：项目类别（web / mobile / desktop / other）
- `techStack`：技术栈字符串数组
- `status`：项目状态（completed / in-progress / planned）
- `liveDemo`：在线演示 URL（可选）
- `sourceCode`：源码仓库 URL（可选）
- `visitUrl`：项目主页 URL（可选）
- `startDate`：开始日期（YYYY-MM-DD）
- `endDate`：结束日期（YYYY-MM-DD，可选）
- `featured`：是否置顶（可选，默认 false）
- `tags`：额外标签字符串数组（可选）
- `showImage`：是否显示封面图（可选，默认 true）

**校验规则**：
- `id` 必填，1-64 字符，只允许字母、数字、连字符、下划线，全局唯一
- `title` 必填，1-255 字符
- `description` 必填，1-2000 字符
- `category` 必填，必须为有效枚举值
- `techStack` 必填，字符串数组，每项 1-50 字符，最多 20 项
- `status` 必填，必须为有效枚举值
- `startDate` 必填，必须为有效日期格式 `YYYY-MM-DD`
- `endDate` 可选，若提供必须 >= `startDate`
- URL 字段（liveDemo / sourceCode / visitUrl）若提供必须是合法 URL

**前端用途**：
- 管理后台创建新项目

---

### 3.4 更新项目

**功能描述**：更新指定 ID 的项目信息。

**需要的数据**：
- 项目 ID（路径参数）
- 可更新字段：title, description, image, category, techStack, status, liveDemo, sourceCode, visitUrl, startDate, endDate, featured, tags, showImage
- `id` 不可变更

**校验规则**：
- 同创建项目的字段校验规则
- 更新前需确认项目存在
- 技术栈和标签采用全量替换策略

**前端用途**：
- 管理后台编辑已有项目

---

### 3.5 删除项目

**功能描述**：删除指定 ID 的项目。

**需要的数据**：
- 项目 ID（路径参数）

**业务规则**：
- 删除项目时级联删除关联的技术栈和标签记录
- 删除项目后检查封面图片是否被其他项目引用，若无引用则清理物理文件

**前端用途**：
- 管理后台删除项目

---

### 3.6 获取所有技术栈列表

**功能描述**：获取所有项目中使用的不重复技术栈列表。

**需要的数据**：
- 去重后的技术栈名称列表
- 按字母排序

**前端用途**：
- 技术栈统计展示
- 技术栈标签云
- 按技术栈筛选项目

---

### 3.7 获取所有项目分类

**功能描述**：获取所有有效的项目分类枚举值。

**需要的数据**：
- 分类值列表：`["web", "mobile", "desktop", "other"]`
- 每个分类对应的项目数量统计（可选）

**前端用途**：
- `FilterTabs` 筛选栏构建类别选项
- 类别图标映射（web -> language, mobile -> smartphone, desktop -> desktop-windows, other -> widgets）

---

### 3.8 获取项目统计数据

**功能描述**：获取项目统计信息。

**需要的数据**：
- `total`：项目总数
- `byStatus`：各状态项目数量（completed, inProgress, planned）
- `byCategory`：各分类项目数量（web, mobile, desktop, other）
- `featuredCount`：置顶项目数量

**前端用途**：
- 统计面板展示
- 页面头部数据概览

---

## 4. 前端架构

### 4.1 文件结构

```
src/
├── data/
│   └── projects.ts              # Project 数据定义与静态数据
├── pages/
│   └── projects.astro           # 项目展示页面（/projects/）
├── components/
│   └── features/
│       ├── projects/
│       │   ├── ProjectCard.astro    # 项目卡片组件
│       │   ├── types.ts             # ProjectCard Props 类型
│       │   └── index.ts             # 模块导出
│       └── featured-projects/
│           ├── FeaturedProjects.astro   # 精选项目展示组件
│           ├── types.ts                 # FeaturedProjects Props 类型
│           └── index.ts                 # 模块导出
```

### 4.2 UI 组件分层

#### 4.2.1 展示层（Public View）

- **ProjectCard**: 项目卡片组件，核心展示单元
  - 封面图区域（条件渲染，支持 `showImage` 控制）
  - 标题 + 状态标签
  - 项目描述（最多两行，超出截断）
  - 技术栈标签（最多展示 `maxTechStack` 个，超出显示 `+N`）
  - 操作按钮区：访问项目、查看源码（条件渲染）
  - 置顶星标（`featured: true` 时显示）
  - 悬停动画效果

- **FeaturedProjects**: 精选项目展示组件
  - 接收 `projects` 数组，通常传入置顶项目
  - 使用大尺寸卡片（`size="large"`）
  - 强制显示封面图（`showImage={true}`）
  - 可配置标题（如 "Featured Projects"）

#### 4.2.2 页面层（Page View）

- **projects.astro**: 项目列表页面
  - 页面头部：标题 + 副标题
  - FilterTabs 筛选栏：按类别筛选（全部 / web / mobile / desktop / other）
  - 项目网格：`grid grid-cols-1 md:grid-cols-2 gap-6`
  - 无结果提示：`#no-results` 占位符

#### 4.2.3 管理后台层（Admin View）

管理后台表单字段：
- 项目 ID（创建时手动输入）
- 项目标题
- 类别下拉框（网页应用/移动应用/桌面应用/其它）
- 状态下拉框（已完成/进行中/已计划）
- 项目描述
- 封面图片路径
- 技术栈（逗号分隔字符串）
- 标签（逗号分隔字符串）
- Demo 网址
- 源码网址
- 项目主页网址
- 开始日期（YYYY-MM-DD）
- 结束日期（YYYY-MM-DD，选填）
- 是否置顶复选框

### 4.3 状态管理

当前 Projects 模块无复杂全局状态管理，完全依赖：

1. **构建时静态数据**：数据在 Astro 构建时从 `projects.ts` 读取并渲染为静态 HTML
2. **客户端 DOM 状态**：筛选状态通过 DOM 类名（`active`、`filtered-out`）管理

---

## 5. 数据流

### 5.1 构建时数据流（Astro 静态生成）

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   src/data/     │     │  projects.astro  │     │   静态 HTML     │
│  projects.ts    │────▶│  (Astro 页面)    │────▶│  /projects/index│
│  (数据源文件)    │     │  构建时执行       │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │  ProjectCard 组件 │
                        │  渲染每个项目卡片  │
                        └──────────────────┘
```

### 5.2 运行时客户端数据流（筛选交互）

```
用户点击筛选标签
       │
       ▼
FilterTabs 按钮点击事件
       │
       ▼
filter-tabs-handler.js
       │
       ├── 切换 active 类
       ├── 读取 data-filter-value
       ├── 匹配 data-category 属性
       └── 添加/移除 .filtered-out 类
       │
       ▼
ProjectCard 显示/隐藏
       │
       ▼
无结果提示显示/隐藏 (#no-results)
```

---

## 6. 业务逻辑规则

### 6.1 Featured 置顶逻辑

- `featured` 为布尔字段，用于标记置顶/精选项目
- 置顶项目在前端展示时显示星标图标
- 置顶项目可被 FeaturedProjects 组件单独展示
- 置顶项目数量建议控制在 2-6 个
- 置顶项目在列表中优先展示

### 6.2 状态流转规则

| 状态 | 含义 | 前端展示 |
|------|------|----------|
| `planned` | 已计划 | "已计划"（灰色/次要样式） |
| `in-progress` | 进行中 | "进行中"（主色/高亮样式） |
| `completed` | 已完成 | "已完成"（成功/绿色样式） |

- 项目状态可在 `planned` / `in-progress` / `completed` 之间自由切换
- 状态变为 `completed` 时，若 `endDate` 为空，建议自动填充当前日期

### 6.3 日期处理逻辑

- `startDate` 必填，`endDate` 可选
- `endDate` 若提供，必须大于等于 `startDate`
- 日期格式统一为 `YYYY-MM-DD`

### 6.4 Tech Stack / Tags 处理逻辑

- 技术栈和标签分别存储
- 创建/更新时采用全量替换策略
- 技术名称/标签名称统一做 `trim()` 处理
- 重复名称在单项目中应去重
- 空数组 `[]` 是合法值

---

## 7. i18n 国际化

### 7.1 使用的 i18n Key

| Key | 用途 |
|-----|------|
| `projects` | 页面标题 |
| `projectsSubtitle` | 页面副标题 |
| `projectsCompleted` | 状态：已完成 |
| `projectsInProgress` | 状态：进行中 |
| `projectsPlanned` | 状态：已计划 |
| `projectsWeb` | 类别：网页应用 |
| `projectsMobile` | 类别：移动应用 |
| `projectsDesktop` | 类别：桌面应用 |
| `projectsOther` | 类别：其它 |
| `projectsVisit` | 按钮：访问项目 |
| `friendsFilterAll` | 筛选：全部 |

### 7.2 类别图标映射

| 类别 | Iconify 图标 |
|------|-------------|
| `web` | `material-symbols:language` |
| `mobile` | `material-symbols:smartphone` |
| `desktop` | `material-symbols:desktop-windows` |
| `other` | `material-symbols:widgets` |

---

## 8. 向后端迁移的影响点

### 8.1 前端需调整的部分

1. **数据获取方式**：从静态导入改为 API 请求（构建时或运行时）
2. **图片路径**：确认后端返回的图片 URL 格式与当前 `/assets/projects/` 一致
3. **ID 类型**：当前为自定义 `string`，后端可能使用 `UUID` 或保留字符串 ID
4. **数据工具函数**：`getProjectStats`, `getProjectsByCategory`, `getFeaturedProjects`, `getAllTechStack` 需改为 API 调用或在前端计算
5. **筛选逻辑**：当前构建时生成筛选标签，迁移后可能需要客户端动态生成

### 8.2 保持不变的特性

1. **Project 接口字段**：建议后端响应结构与当前 TS 接口保持一致
2. **ProjectCard 组件**：展示逻辑无需修改，只需调整数据获取方式
3. **筛选交互**：`filter-tabs-handler.js` 的 DOM 筛选逻辑可复用
4. **i18n Key**：所有国际化键值保持不变
5. **管理后台表单字段**：表单字段与后端 API 字段一一对应

---

## 9. 附录

### 9.1 术语表

| 术语 | 说明 |
|------|------|
| Featured | 置顶/精选项目，前端显示星标并优先展示 |
| Tech Stack | 技术栈，项目使用的技术框架/语言列表 |
| Status | 项目状态，表示项目的开发进度 |
| Category | 项目类别，用于分组和筛选 |
| FilterTabs | 筛选标签组件，支持按属性值过滤列表 |

### 9.2 相关文档

- [api-projects.md](./api-projects.md) - Projects 模块后端 API 需求文档
