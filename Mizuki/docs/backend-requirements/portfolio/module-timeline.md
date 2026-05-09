# Timeline（时间线）模块描述文档

> **模块名称**: Timeline
> **前端页面**: `src/pages/timeline.astro`
> **前端组件**: `src/components/features/timeline/TimelineCard.astro`
> **前端数据文件**: `src/data/timeline.ts`
> **文档版本**: v1.0
> **最后更新**: 2026-05-04

---

## 1. 模块概述

Timeline 模块是 Mizuki 博客系统中用于展示个人履历、成长轨迹与关键经历的模块。它以时间轴（Timeline）的形式，按时间倒序排列用户的教育背景、工作经历、项目经历、成就奖项及其他重要事件，为访客提供直观的个人发展历程视图。

### 1.1 核心目标

- 以可视化时间轴形式展示个人关键经历
- 支持按事件类型（教育/工作/项目/成就）分类展示与筛选
- 提供管理后台，支持事件的增删改查
- 通过 `featured` 标记突出展示重要事件
- 支持展示事件的详细元数据：技能标签、成就列表、外部链接、地点、机构等

### 1.2 用户角色

| 角色 | 权限 |
|------|------|
| 访客 | 查看公开的时间线事件 |
| 管理员 | 创建、编辑、删除时间线事件 |

---

## 2. 数据结构

### 2.1 核心接口定义

文件路径：`src/components/features/timeline/types.ts`

```typescript
export interface TimelineLink {
  name: string;
  url: string;
  type: "website" | "certificate" | "project" | "other";
}

export interface TimelineItem {
  id: string;                          // 唯一标识符
  title: string;                       // 事件标题
  description: string;                 // 事件详细描述
  type: "education" | "work" | "project" | "achievement";  // 事件类型
  startDate: string;                   // 开始日期，格式 YYYY-MM-DD
  endDate?: string;                    // 结束日期，格式 YYYY-MM-DD（可选）
  location?: string;                   // 地点（可选）
  organization?: string;               // 所属机构/组织（可选）
  position?: string;                   // 职位/角色（可选）
  skills?: string[];                   // 关联技能标签列表（可选）
  achievements?: string[];             // 成就/成果列表（可选）
  links?: TimelineLink[];              // 相关链接列表（可选）
  icon?: string;                       // 图标标识（可选，前端有默认值）
  color?: string;                      // 主题色（可选，前端有默认值）
  featured?: boolean;                  // 是否为重点展示事件（可选，默认 false）
}
```

### 2.2 字段详细说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 全局唯一标识，建议使用 UUID 或数据库自增 ID 的字符串形式 |
| `title` | `string` | 是 | 事件标题，如 "计算机科学学士学位"、"前端开发实习" |
| `description` | `string` | 是 | 事件的详细描述，支持多行文本 |
| `type` | `enum` | 是 | 事件类型：`education`（教育）、`work`（工作）、`project`（项目）、`achievement`（成就） |
| `startDate` | `string` | 是 | 事件开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 否 | 事件结束日期，格式 `YYYY-MM-DD`。无结束日期表示进行中 |
| `location` | `string` | 否 | 地理位置，如 "北京"、"济南，山东" |
| `organization` | `string` | 否 | 机构名称，如 "北京理工大学"、"TechStart Internet Company" |
| `position` | `string` | 否 | 职位或角色，如 "前端开发实习生"、"编程助教" |
| `skills` | `string[]` | 否 | 关联技能数组，如 `["React", "JavaScript", "Git"]` |
| `achievements` | `string[]` | 否 | 成就/成果列表，如 `["获得优秀实习生证书", "完成用户界面组件开发"]` |
| `links` | `TimelineLink[]` | 否 | 相关外部链接，如项目仓库、证书验证页、课程链接 |
| `icon` | `string` | 否 | 图标标识符，如 `material-symbols:school`。为空时前端按类型默认映射 |
| `color` | `string` | 否 | HEX 颜色值，如 `#2A53DD`。为空时前端按类型默认映射 |
| `featured` | `boolean` | 否 | 是否为精选事件，默认 `false`。前端对 featured 事件显示星标 |

### 2.3 类型与视觉映射

当前前端在展示时，若 `icon` 或 `color` 未提供，根据 `type` 自动映射默认值：

| 类型值 | 类型含义 | 默认图标 (`icon`) | 默认颜色 (`color`) |
|--------|----------|-------------------|-------------------|
| `education` | 教育背景 | `material-symbols:school` | `#2A53DD` |
| `work` | 工作经历 | `material-symbols:work` | `#51F56C` |
| `project` | 项目经历 | `material-symbols:code-blocks` | `#FF4B63` |
| `achievement` | 成就奖项 | `material-symbols:emoji-events` | `#FAB83E` |

> **注意**: 前端允许自定义 `icon` 和 `color` 覆盖默认值。后端需存储实际使用的 `icon` 和 `color` 值；若前端未提供，后端可根据类型自动填充默认值。

### 2.4 数据示例

```typescript
{
  id: "current-study",
  title: "Studying Computer Science and Technology",
  description: "Currently studying Computer Science and Technology, focusing on web development and software engineering.",
  type: "education",
  startDate: "2022-09-01",
  location: "Beijing",
  organization: "Beijing Institute of Technology",
  skills: ["Java", "Python", "JavaScript", "HTML/CSS", "MySQL"],
  achievements: [
    "Current GPA: 3.6/4.0",
    "Completed data structures and algorithms course project",
    "Participated in multiple course project developments",
  ],
  icon: "material-symbols:school",
  color: "#059669",
  featured: true,
}
```

---

## 3. 前端架构

### 3.1 文件结构

```
src/
├── data/
│   └── timeline.ts                    # Timeline 数据定义与静态数据
├── pages/
│   └── timeline.astro                 # 时间线展示页面（/timeline/）
├── components/
│   └── features/
│       └── timeline/
│           ├── TimelineCard.astro     # 单个时间线事件卡片
│           ├── types.ts               # TimelineItem / TimelineCardProps 类型
│           └── index.ts               # 模块导出
└── scripts/
    └── filter-tabs-handler.js         # 筛选标签交互逻辑
```

### 3.2 UI 组件分层

#### 3.2.1 展示层（Public View）

- **TimelineCard**: 事件卡片组件，核心展示单元
  - 时间轴节点（圆点）：根据 `color` 渲染，进行中事件带脉冲动画
  - 标题 + 星标（`featured: true` 时显示）
  - 机构名称 + 职位（如有）
  - 事件描述
  - 技能标签列表
  - 成就列表（带勾选图标）
  - 日期范围 + 持续时长 + 地点
  - 相关链接按钮区（条件渲染）
  - 类型标签（右上角）
  - 悬停动画效果

- **FilterTabs**: 筛选标签栏
  - 全部 / 教育 / 工作 / 项目 / 成就
  - 显示每个类型的数量统计
  - 点击后通过 DOM 类名切换显示/隐藏

#### 3.2.2 页面层（Page View）

- **timeline.astro**: 时间线列表页面
  - 页面头部：标题 + 副标题
  - FilterTabs 筛选栏
  - 时间轴列表：左侧竖线 + 右侧卡片
  - 无结果提示：`#no-results` 占位符

#### 3.2.3 管理后台层（Admin View）

基于 `app.py` 中 Gradio 构建的管理界面：

- **事件选择下拉框**：选择已有事件进行编辑
- **表单字段区**：
  - 事件 ID
  - 事件标题
  - 事件类型下拉框（教育/工作/项目/成就）
  - 开始日期（YYYY-MM-DD）
  - 结束日期（YYYY-MM-DD，选填）
  - 地点
  - 机构名称
  - 职位/角色
  - 事件描述
  - 技能标签（逗号分隔字符串）
  - 成就列表（多行文本或逗号分隔）
  - 相关链接
  - 是否重点展示复选框
- **操作按钮**：保存修改、创建、删除选中的事件

### 3.3 状态管理

当前 Timeline 模块**无复杂全局状态管理**，完全依赖：

1. **构建时静态数据**：数据在 Astro 构建时从 `timeline.ts` 读取并渲染为静态 HTML
2. **客户端 DOM 状态**：筛选状态通过 DOM 类名（`active`、`filtered-out`）管理
3. **管理后台状态**：通过 Gradio 的 `gr.State` 组件在 Python 侧维护数据状态

---

## 4. 数据流

### 4.1 构建时数据流（Astro 静态生成）

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   src/data/     │     │  timeline.astro  │     │   静态 HTML     │
│  timeline.ts    │────▶│  (Astro 页面)    │────▶│  /timeline/index│
│  (数据源文件)    │     │  构建时执行       │     │                 │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │  TimelineCard 组件│
                        │  渲染每个事件卡片  │
                        └──────────────────┘
```

### 4.2 页面数据获取流程

1. **页面加载阶段**：
   ```astro
   ---
   import { timelineData } from "../data/timeline";
   ---
   ```

2. **筛选标签构建**：
   ```astro
   const types = ["education", "work", "project", "achievement"] as const;
   const filterTabs = [
     { value: "all", label: "全部", count: timelineData.length },
     ...types.map((type) => ({
       value: type,
       label: getTypeText(type),
       icon: getTypeIcon(type),
       count: timelineData.filter((item) => item.type === type).length,
     })),
   ];
   ```

3. **时间轴列表渲染**：
   ```astro
   <div id="timeline-list" class="timeline-list">
     {timelineData.map((item) => <TimelineCard item={item} />)}
   </div>
   ```

### 4.3 运行时客户端数据流（筛选交互）

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
       ├── 匹配 data-type 属性
       └── 添加/移除 .filtered-out 类
       │
       ▼
TimelineCard 显示/隐藏
       │
       ▼
无结果提示显示/隐藏 (#no-results)
```

---

## 5. UI 组件详解

### 5.1 TimelineCard 组件

**文件路径**：`src/components/features/timeline/TimelineCard.astro`

#### Props 接口

```typescript
interface TimelineCardProps {
  item: TimelineItem;              // 事件数据对象
  maxSkills?: number;              // 最多展示的技能数量
}
```

#### 组件结构

```
TimelineCard (div.timeline-entry)
├── Timeline Node (div.timeline-node)
│   └── 图标 (iconify-icon, 根据 type/icon 渲染)
├── Timeline Card (div.timeline-card)
│   ├── Header
│   │   ├── 图标区域 (w-10 h-10 圆角)
│   │   ├── 标题 (h3) + 星标 (featured)
│   │   └── 机构 + 职位 (organization · position)
│   ├── 类型标签 + 当前状态标签 (isCurrent)
│   ├── 描述 (p)
│   ├── 技能标签 (flex wrap)
│   ├── 成就列表 (ul, 带勾选图标)
│   ├── 元信息栏 (日期范围 · 持续时长 · 地点)
│   └── 链接按钮区 (条件渲染: links)
└── Hover Gradient Overlay
```

#### 状态计算逻辑

| 条件 | 计算方式 |
|------|----------|
| `isCurrent` | `!item.endDate`，无结束日期表示进行中 |
| `dateRange` | `formatDate(startDate) - formatDate(endDate)`，无 endDate 显示 "Present" |
| `duration` | 计算 startDate 到 endDate（或当前日期）的月数/年数 |
| `nodeColor` | `isCurrent ? "#22c55e" : item.color` |
| `icon` | `item.icon \|\| getTypeIcon(item.type)` |

#### 动画效果

1. **入场动画**：`fadeInUp` 关键帧动画
   - 初始：`opacity: 0`, `translateY(16px)`
   - 结束：`opacity: 1`, `translateY(0)`
   - 持续时间：0.5s，缓动：ease-out
   - 交错延迟：`nth-child(1~9)` 分别延迟 0.05s ~ 0.45s

2. **悬停效果**：
   - 节点缩放：`transform: scale(1.15)`
   - 节点阴影变化
   - 卡片阴影增强：`hover:shadow-lg`
   - 渐变遮罩显示

3. **进行中脉冲**：`.is-current .timeline-node` 带 `pulse` 动画

4. **筛选隐藏**：`.filtered-out` 类设置 `display: none`

---

## 6. 交互流程

### 6.1 加载时间线列表

1. 访客进入 Timeline 页面
2. 页面在构建时已渲染静态 HTML
3. 客户端加载 `filter-tabs-handler.js` 初始化筛选交互
4. Iconify 图标库异步加载

### 6.2 筛选时间线事件

1. 用户点击筛选标签（全部 / 教育 / 工作 / 项目 / 成就）
2. `filter-tabs-handler.js` 读取点击标签的 `data-filter-value`
3. 遍历所有 `.timeline-entry` 元素
4. 匹配 `data-type` 属性，添加/移除 `.filtered-out` 类
5. 若无匹配项，显示 `#no-results` 提示

### 6.3 管理后台：选择事件

1. 管理员从下拉框选择事件
2. 解析选项字符串获取 `id`
3. 从数据数组中查找匹配事件
4. 返回所有字段填充表单

### 6.4 管理后台：保存/创建事件

1. 管理员填写表单字段
2. 选择 `type` 时，前端自动映射并填充默认 `icon` 和 `color`
3. 点击保存
4. 前端校验表单数据
5. 判断新建或更新：
   - 若 `id` 在现有数据中不存在 -> 追加到数组（新建）
   - 若 `id` 已存在 -> 替换对应事件（更新）
6. 回写 `timeline.ts` 文件
7. 刷新 UI

### 6.5 管理后台：删除事件

1. 管理员选择事件后点击删除
2. 在数据数组中查找并移除匹配事件
3. 回写文件
4. 刷新 UI

---

## 7. 样式系统

### 7.1 CSS 变量依赖

| 变量 | 用途 |
|------|------|
| `--primary` | 主题主色，用于技能标签、类型标签、悬停效果 |
| `--line-divider` | 时间轴线颜色 |
| `--radius-large` | 大圆角半径（卡片圆角） |
| `--btn-regular-bg` | 操作按钮背景 |

### 7.2 Tailwind 类使用

- **布局**：`flex`, `gap-3`, `p-5`, `px-6`
- **响应式**：`sm:px-9`
- **颜色**：`text-black/90`, `dark:text-white/90`, `bg-[var(--primary)]/10`
- **交互**：`hover:shadow-lg`, `transition-all`, `group-hover`
- **溢出**：`overflow-hidden`, `truncate`

---

## 8. i18n 国际化

### 8.1 使用的 i18n Key

| Key | 用途 | 文件位置 |
|-----|------|----------|
| `timeline` | 页面标题 | `src/i18n/languages/*.ts` |
| `timelineSubtitle` | 页面副标题 | `src/i18n/languages/*.ts` |
| `timelineEducation` | 类型：教育 | `src/i18n/languages/*.ts` |
| `timelineWork` | 类型：工作 | `src/i18n/languages/*.ts` |
| `timelineProject` | 类型：项目 | `src/i18n/languages/*.ts` |
| `timelineAchievement` | 类型：成就 | `src/i18n/languages/*.ts` |
| `timelinePresent` | 日期：至今 | `src/i18n/languages/*.ts` |
| `timelineCurrent` | 标签：进行中 | `src/i18n/languages/*.ts` |
| `timelineMonths` | 时长：月 | `src/i18n/languages/*.ts` |
| `timelineYears` | 时长：年 | `src/i18n/languages/*.ts` |
| `timelineAchievements` | 成就标题 | `src/i18n/languages/*.ts` |
| `friendsFilterAll` | 筛选：全部 | `src/i18n/languages/*.ts` |

### 8.2 类型图标映射

| 类型 | Iconify 图标 |
|------|-------------|
| `education` | `material-symbols:school` |
| `work` | `material-symbols:work` |
| `project` | `material-symbols:code` |
| `achievement` | `material-symbols:emoji-events` |

---

## 9. 依赖关系

### 9.1 文件依赖图

```
src/pages/timeline.astro
├── @components/atoms/filter-tabs/FilterTabs.astro
├── @components/features/page-header/PageHeader.astro
├── @components/features/timeline/TimelineCard.astro
│   └── ./types.ts (TimelineCardProps)
│       └── TimelineItem interface
├── @layouts/MainGridLayout.astro
├── ../config.ts (siteConfig.featurePages.timeline)
├── ../data/timeline.ts (timelineData)
├── ../i18n/i18nKey.ts
└── ../i18n/translation.ts (i18n function)

客户端运行时：
├── /js/filter-tabs-handler.js (筛选交互)
└── Iconify (图标渲染)
```

### 9.2 外部库依赖

- **Astro**：框架核心，静态生成页面
- **Tailwind CSS**：样式工具
- **Iconify**：图标渲染（`<iconify-icon>`）
- **Swup**：页面过渡（filter-tabs-handler 兼容 `astro:page-load` 事件）

---

## 10. 性能考量

### 10.1 当前优化措施

1. **静态生成**：页面在构建时生成，无运行时数据获取开销
2. **CSS 动画优化**：使用 `transform` 和 `opacity` 实现动画，避免重排
3. **条件渲染**：无技能/成就/链接的事件不渲染对应区域

### 10.2 潜在优化点

1. **分页加载**：事件数量增多时，应考虑分页或无限滚动
2. **构建性能**：`timelineData` 在多个页面/组件中被引用，构建时会重复处理

---

## 11. 向后端迁移的影响点

### 11.1 前端需调整的部分

1. **数据获取方式**：从静态导入改为 API 请求（构建时或运行时）
2. **ID 类型**：当前为自定义 `string`，后端可能使用 `UUID` 或保留字符串 ID
3. **筛选逻辑**：当前构建时生成筛选标签和数量统计，迁移后可能需要客户端动态生成或后端提供统计接口
4. **排序逻辑**：当前前端按数组顺序展示，迁移后需确认后端默认排序规则（按日期倒序、featured 优先）

### 11.2 保持不变的特性

1. **TimelineItem 接口字段**：建议后端响应结构与当前 TS 接口保持一致
2. **TimelineCard 组件**：展示逻辑无需修改，只需调整数据获取方式
3. **筛选交互**：`filter-tabs-handler.js` 的 DOM 筛选逻辑可复用
4. **i18n Key**：所有国际化键值保持不变
5. **管理后台表单字段**：`app.py` 中的表单字段与后端 API 字段一一对应

---

## 12. 附录

### 12.1 术语表

| 术语 | 说明 |
|------|------|
| Iconify | 前端图标库，支持 `material-symbols:` 等前缀的图标标识 |
| Featured | 精选/重点事件，前端显示星标并优先展示 |
| Timeline | 时间线/时间轴，按时间顺序展示事件的 UI 组件 |
| TimelineLink | 时间线事件关联的外部链接，包含名称、URL 和类型 |
| isCurrent | 进行中事件，无 `endDate` 的事件被视为当前正在进行 |

### 12.2 相关文档

- [api-timeline.md](./api-timeline.md) - Timeline 模块后端 API 需求文档
