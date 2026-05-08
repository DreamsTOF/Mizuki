# Friends（友情链接）模块描述文档

> **模块名称**: Friends
> **前端页面**: `src/pages/friends.astro`
> **前端组件**: `src/components/features/friends/FriendCard.astro`
> **前端数据文件**: `src/data/friends.ts`
> **文档版本**: v2.0
> **最后更新**: 2026-05-04

---

## 1. 模块概述

Friends 模块是 Mizuki 博客的友情链接展示与管理页面，以卡片网格形式展示所有友链，支持搜索、标签筛选、随机排序、访问链接和复制链接等功能。

### 1.1 功能特性

- **友链卡片网格展示**：以响应式网格布局展示所有友情链接
- **随机排序**：每次页面加载时友链顺序随机打乱
- **实时搜索**：支持按友链标题和描述进行实时搜索过滤
- **标签筛选**：支持按标签分类筛选友链
- **访问链接**：点击按钮在新标签页打开友链网站
- **复制链接**：一键复制友链网站 URL 到剪贴板
- **响应式设计**：适配桌面端（3列）、平板端（2列）和移动端（1列）
- **动画效果**：卡片入场动画、悬停上浮效果、图片缩放、渐变遮罩
- **i18n 国际化**：完整的多语言支持

### 1.2 页面开关配置

在 `src/config.ts` 中通过 `featurePages.friends` 控制该页面的启用/禁用：

```typescript
featurePages: {
    friends: true, // 友链页面开关
}
```

当设置为 `false` 时，访问 `/friends/` 路径会自动重定向到 `/404/`。

---

## 2. 数据结构

### 2.1 核心接口定义

基于 `src/data/friends.ts`：

```typescript
interface FriendItem {
  id: number;          // 友链唯一标识符
  title: string;       // 友链网站标题/名称
  imgurl: string;      // 头像/Logo 图片 URL（支持外链或本地上传）
  desc: string;        // 友链网站描述
  siteurl: string;     // 网站链接
  tags: string[];      // 标签数组
}
```

### 2.2 字段详细说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `number` | 是 | 友链唯一标识符，递增整数 |
| `title` | `string` | 是 | 友链网站标题/名称 |
| `imgurl` | `string` | 是 | 头像/Logo 图片 URL，支持外链（如 GitHub avatars）或本地路径 |
| `desc` | `string` | 是 | 友链网站描述 |
| `siteurl` | `string` | 是 | 友链网站链接，需为有效 URL |
| `tags` | `string[]` | 是 | 标签数组，用于分类和筛选 |

### 2.3 数据示例

```typescript
{
    id: 1,
    title: "Astro",
    imgurl: "https://avatars.githubusercontent.com/u/44914786?v=4&s=640",
    desc: "The web framework for content-driven websites",
    siteurl: "https://github.com/withastro/astro",
    tags: ["Framework"],
}
```

---

## 3. 前端架构

### 3.1 文件结构

```
src/
├── pages/
│   └── friends.astro              # 友链页面
├── components/
│   └── features/
│       └── friends/
│           └── FriendCard.astro   # 友链卡片组件
├── data/
│   └── friends.ts                 # 友链数据与接口定义
└── app.py                         # 管理后台（模块划分参考）
```

### 3.2 UI 组件分层

#### 3.2.1 友链页面（friends.astro）

- **PageHeader**: 页面头部，展示标题 "友链" 和副标题 "发现更多优质网站"
- **搜索框**: 按友链名称或描述实时搜索过滤
- **标签筛选栏**: 展示所有标签按钮，支持 "全部" 和按具体标签筛选
- **友链卡片网格**: 响应式网格布局，渲染 FriendCard 列表
- **无结果提示**: 搜索/筛选无匹配时展示
- **说明文档**: 页面底部 Markdown 说明内容

#### 3.2.2 友链卡片（FriendCard.astro）

- **头像与标题区**: 左侧 64x64px 头像，右侧标题和域名链接
- **描述**: 最多两行文本截断
- **标签区**: 展示该友链的所有标签
- **操作按钮**: "访问" 链接按钮 + "复制链接" 按钮
- **悬停效果**: 卡片上浮、阴影增强、图片缩放、渐变遮罩

### 3.3 状态管理

当前前端状态（基于 Astro 静态生成）：

```typescript
// 构建时状态
interface FriendsBuildState {
  friendsList: FriendItem[];       // 所有友链数据（随机排序后）
  allTags: string[];               // 所有去重后的标签
}

// 客户端运行时状态（friends-page-handler.js）
interface FriendsPageState {
  initialized: boolean;            // 是否已初始化
  eventListeners: EventListener[]; // 事件监听器引用数组
  copySuccessText: string;         // 复制成功提示文本
}
```

---

## 4. 数据流

### 4.1 构建时数据流（Astro 静态生成）

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   src/data/     │     │  friends.astro   │     │   静态 HTML     │
│   friends.ts    │────▶│  (Astro 页面)    │────▶│   输出文件      │
│  (数据源文件)    │     │  构建时执行       │     │ /friends/index  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                               │
                               ▼
                        ┌──────────────────┐
                        │  FriendCard 组件  │
                        │  渲染每个友链条目  │
                        └──────────────────┘
```

1. 页面构建时调用 `getShuffledFriendsList()` 获取随机排序的友链数据
2. 从所有友链的 `tags` 字段提取去重，生成标签筛选按钮
3. 渲染为静态 HTML，包含所有友链卡片和交互元素

### 4.2 运行时客户端数据流

```
用户输入搜索词 / 点击标签
       │
       ▼
friends-page-handler.js
       │
       ├── 搜索：读取 input 值，匹配 data-title / data-desc
       ├── 标签：读取 data-tag，匹配 data-tags
       └── 综合过滤：同时满足搜索和标签条件才显示
       │
       ▼
FriendCard 显示/隐藏 (style.display)
       │
       ▼
无结果提示显示/隐藏 (#no-results)
```

---

## 5. 交互流程

### 5.1 页面加载

1. 构建时获取友链列表（随机排序）
2. 提取所有标签并去重
3. 渲染搜索框、标签筛选按钮、友链卡片网格
4. 客户端脚本初始化搜索和筛选事件监听

### 5.2 搜索过滤

1. 用户在搜索框输入关键词
2. 实时遍历所有 FriendCard 的 `data-title` 和 `data-desc` 属性
3. 包含关键词的卡片显示，不匹配的隐藏
4. 同时结合当前选中的标签进行双重过滤

### 5.3 标签筛选

1. 用户点击标签按钮
2. 切换按钮的 `active` 状态
3. 若选中 "全部"，显示所有卡片
4. 若选中具体标签，遍历卡片的 `data-tags` 属性进行匹配
5. 同时结合搜索框内容进行双重过滤

### 5.4 复制链接

1. 用户点击复制按钮
2. 调用 `navigator.clipboard.writeText(url)`
3. 成功时按钮显示 "已复制" 提示，2秒后恢复

---

## 6. 排序与展示规则

### 6.1 随机排序

- 页面构建时使用 Fisher-Yates 洗牌算法打乱友链顺序
- 每次构建/刷新产生不同顺序
- 前端展示时保持该随机顺序不变（除非后端支持运行时随机）

### 6.2 卡片布局

- 桌面端（xl+）：3 列网格
- 平板端（sm+）：2 列网格
- 移动端：1 列网格

---

## 7. 样式系统

### 7.1 CSS 变量依赖

| 变量 | 用途 |
|------|------|
| `--primary` | 主题主色，用于标签背景、按钮背景、悬停文字色 |
| `--radius-large` | 大圆角半径 |
| `--btn-regular-bg` | 复制按钮背景 |

### 7.2 Tailwind 类使用

- **布局**：`grid`, `flex`, `gap-*`, `p-*`, `grid-cols-*`
- **响应式**：`sm:grid-cols-2`, `xl:grid-cols-3`
- **交互**：`hover:shadow-xl`, `hover:-translate-y-1`, `transition-all`
- **溢出**：`overflow-hidden`, `truncate`, `line-clamp-2`

---

## 8. 依赖关系

### 8.1 文件依赖图

```
src/pages/friends.astro
├── @components/features/friends/FriendCard.astro
├── @components/features/page-header (PageHeader)
├── @components/misc/Markdown.astro
├── @layouts/MainGridLayout.astro
├── ../config.ts (siteConfig)
├── ../data/friends.ts (getShuffledFriendsList, FriendItem)
├── ../i18n/i18nKey.ts
└── ../i18n/translation.ts (i18n function)

FriendCard.astro
├── ../../../i18n/i18nKey.ts
└── ../../../i18n/translation.ts (i18n function)

客户端运行时：
└── /js/friends-page-handler.js (搜索、筛选、复制交互)
```

---

## 9. 向后端迁移的影响点

### 9.1 前端需调整的部分

1. **数据获取方式**：从静态导入改为 API 请求（构建时或运行时）
2. **随机排序**：当前在构建时随机，后端迁移后可改为前端随机或后端支持随机参数
3. **图片路径**：确认后端返回的图片 URL 格式与当前一致
4. **ID 类型**：当前为 `number`，后端可能使用 `bigint` 或 `string`（UUID）

### 9.2 保持不变的特性

1. **FriendItem 接口字段**：建议后端响应结构与当前 TS 接口保持一致
2. **筛选交互**：搜索框 + 标签筛选 + friends-page-handler.js 无需修改
3. **FriendCard 渲染逻辑**：只要数据格式一致，组件无需改动
4. **复制链接功能**：完全客户端实现，不受后端影响
