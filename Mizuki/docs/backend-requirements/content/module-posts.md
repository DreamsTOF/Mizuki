# Posts（文章）模块需求文档

> **模块名称**: Posts
> **前端页面**: `src/pages/posts/[...slug].astro`, `src/pages/archive.astro`
> **前端组件**: `src/components/features/posts/` 下的 PostCard, PostMeta, PostNavigation, RelatedPosts, EncryptedBadge 等
> **文档版本**: v1.0
> **最后更新**: 2026-05-04

---

## 1. 模块概述

Posts 模块是 Mizuki 博客系统的核心内容模块，负责博客文章的存储、展示与管理。文章以 Markdown 格式存储，支持丰富的 Frontmatter 元数据，涵盖基础信息、状态控制、自定义链接、加密等能力。

### 1.1 用户角色

| 角色 | 权限 |
|------|------|
| 访客 | 查看已发布的非草稿文章；加密文章需验证密码后查看正文 |
| 管理员 | 创建、编辑、删除文章；管理草稿、置顶、加密状态；上传封面图片 |

---

## 2. 数据结构

### 2.1 文章 Frontmatter

```yaml
title: string                    # 文章标题
published: string                # 发布日期，YYYY-MM-DD
updated?: string                 # 更新日期，YYYY-MM-DD
description?: string             # 文章描述/摘要
tags?: string[]                  # 标签数组
category?: string                # 分类
author?: string                  # 作者名
permalink?: string               # 自定义固定链接
pinned?: boolean                 # 是否置顶，默认 false
draft?: boolean                  # 是否为草稿，默认 false
image?: string                   # 封面图片路径
lang?: string                    # 语言代码
priority?: number                # 置顶优先级，数值越小越靠前

# 加密相关
encrypted?: boolean              # 是否加密，默认 false
password?: string                # 加密密码
passwordHint?: string            # 密码提示

# 许可证与来源
licenseName?: string             # 许可证名称
licenseUrl?: string              # 许可证链接
sourceLink?: string              # 原文链接

# 别名与评论
alias?: string                   # 文章别名
comment?: boolean                # 是否启用评论，默认 true
```

### 2.2 正文格式

- 正文为 Markdown 格式
- 前端渲染器负责将 Markdown 转换为 HTML
- 支持标准 Markdown、GFM、代码高亮、Mermaid 图表等扩展语法

### 2.3 封面图片路径规则

| 格式 | 示例 | 说明 |
|------|------|------|
| 相对路径 | `./cover.jpg` | 相对于文章所在目录的本地图片 |
| Public 路径 | `/images/cover.png` | 指向 `public/` 目录下的静态资源 |
| 外链 | `https://example.com/cover.webp` | 外部图片 URL |

---

## 3. 前端功能需求

### 3.1 获取文章列表

**功能说明**: 获取文章列表，用于首页分页展示、分类/标签筛选等场景。

**需要的数据**:
- 文章 ID
- 标题
- 发布日期
- 更新日期（可选）
- 描述
- 标签数组
- 分类
- 作者
- 置顶状态
- 置顶优先级
- 草稿状态
- 加密状态
- 封面图片路径
- 字数统计（构建时生成）
- 阅读时间（构建时生成）
- 摘要/Excerpt（构建时生成）
- 文章 URL

**筛选与排序需求**:
- 支持分页（页码 + 每页数量）
- 按分类筛选
- 按标签筛选
- 草稿过滤（生产环境默认过滤草稿，管理员可查看全部）
- 置顶排序：置顶文章优先，同置顶按 priority 升序，其余按发布日期降序

### 3.2 获取单篇文章详情

**功能说明**: 获取单篇文章的完整信息，用于文章详情页渲染。

**需要的数据**:
- 文章 ID
- 标题
- Markdown 正文内容
- 所有 Frontmatter 字段
- 字数统计
- 阅读时间
- 摘要/Excerpt
- 文章 URL
- 上一篇文章信息（ID、标题、Slug）
- 下一篇文章信息（ID、标题、Slug）

**特殊处理**:
- 加密文章：未验证密码时不返回正文内容，仅返回元数据和密码提示

### 3.3 创建文章

**功能说明**: 管理员创建新文章。

**需要的数据**:
- 标题（必填）
- Markdown 正文（必填）
- 发布日期（必填）
- 描述
- 标签数组
- 分类
- 作者
- 自定义固定链接
- 置顶状态
- 草稿状态
- 加密状态及密码、密码提示
- 封面图片
- 语言代码
- 许可证信息
- 原文链接
- 是否启用评论

**业务规则**:
- 根据标题自动生成 Slug
- 支持通过 permalink 或 alias 自定义 URL
- 上传封面图片时自动转换为 JPG 格式

### 3.4 更新文章

**功能说明**: 管理员修改已有文章。

**需要的数据**:
- 与创建文章相同的全量字段
- 支持部分字段更新（如仅修改置顶状态、仅更新正文等）
- 支持替换封面图片
- 支持导入外部 Markdown 文件替换正文

### 3.5 删除文章

**功能说明**: 管理员删除文章。

**需要的数据**:
- 文章标识（ID 或 Slug）

### 3.6 上传文章封面图片

**功能说明**: 为文章上传或替换封面图片。

**需要的数据**:
- 文章标识
- 图片文件
- 图片格式限制：jpg、png、webp、gif
- 自动转换为 JPG 格式
- 返回图片访问路径

### 3.7 获取所有文章分类

**功能说明**: 获取所有已使用的分类列表及每类文章数量。

**需要的数据**:
- 分类名称
- 该分类下的文章数量

### 3.8 获取所有文章标签

**功能说明**: 获取所有已使用的标签列表及每个标签的文章数量。

**需要的数据**:
- 标签名称
- 使用该标签的文章数量

### 3.9 获取文章归档

**功能说明**: 按年月分组统计文章，用于归档页面展示。

**需要的数据**:
- 按年份分组的文章列表
- 每篇文章的标题、发布日期（月-日）、URL、标签
- 每年的文章数量
- 支持按分类/标签筛选后重新分组

### 3.10 获取相关文章推荐

**功能说明**: 基于当前文章推荐相关文章。

**需要的数据**:
- 当前文章标识
- 返回相关文章列表（ID、标题、URL、发布日期）

**推荐算法需求**:
- 基于标签 Jaccard 相似度评分
- 基于标题分词 Jaccard 相似度评分
- 时间新鲜度评分（6 个月半衰期指数衰减）
- 同分类加分
- 优先返回有标签匹配的文章，不足时按时间新鲜度补充

---

## 4. 排序与展示规则

### 4.1 文章列表排序

排序优先级（从高到低）：

1. **置顶状态**: `pinned = true` 的文章排在前面
2. **置顶优先级**: 同为置顶文章，按 `priority` 升序排列（数值越小越靠前）
3. **发布日期**: 非置顶文章按 `published` 降序排列（最新的在前）

### 4.2 草稿过滤

- 公开接口默认不返回草稿文章
- 管理员可查看包含草稿的完整列表

### 4.3 相邻文章关联

排序完成后，为每篇文章计算相邻文章：

- `nextSlug` / `nextTitle`: 发布时间更新的文章（列表中的前一篇）
- `prevSlug` / `prevTitle`: 发布时间更旧的文章（列表中的后一篇）

---

## 5. URL 与路由系统

### 5.1 多路由策略

按优先级排列：

| 策略 | 条件 | 路由示例 |
|------|------|----------|
| 自定义 Permalink | `permalink` 存在 | `/my-custom-post/` |
| 全局 Permalink | 全局配置启用 | `/2024-my-post/` |
| 默认 Slug | 以上都不满足 | `/posts/guide/` |
| Alias | `alias` 存在且全局 Permalink 未启用 | `/posts/my-alias/` |

### 5.2 Permalink 占位符

全局 Permalink 格式模板支持以下占位符：

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `%year%` | 发布年份 | `2024` |
| `%monthnum%` | 发布月份（补零） | `04` |
| `%day%` | 发布日期（补零） | `01` |
| `%hour%` | 发布小时（补零） | `09` |
| `%minute%` | 发布分钟（补零） | `30` |
| `%second%` | 发布秒（补零） | `00` |
| `%post_id%` | 文章序号（按发布时间升序，从1开始） | `42` |
| `%postname%` | 文件名（小写，去扩展名） | `my-post` |
| `%raw_postname%` | 原始文件名（保留大小写） | `My-Post` |
| `%category%` | 分类名 | `frontend` |

---

## 6. 加密文章机制

### 6.1 加密流程

1. 文章设置为加密状态并设置密码
2. 公开获取文章详情时，不返回正文内容，仅返回密码提示
3. 访客提交密码验证通过后，返回正文内容
4. 前端使用 CryptoJS 进行 AES 解密渲染

### 6.2 加密文章特性

- 列表中显示锁图标
- 分享卡片、License、评论等组件对加密内容隐藏
- RSS/Atom 中加密文章只暴露元数据，不暴露正文

---

## 7. 图片处理

### 7.1 封面图片

- 支持上传 jpg、png、webp、gif 格式
- 上传后自动转换为 JPG 格式
- 支持相对路径、Public 路径、外链三种引用方式
- 支持响应式图片（多尺寸 srcset）

### 7.2 文章内图片

- 通过标准 Markdown 语法引用
- 支持相对路径和外链
- 前端实现懒加载和响应式

---

## 8. 依赖关系

### 8.1 前端文件依赖

```
src/pages/[...page].astro           # 首页文章列表（分页）
src/pages/posts/[...slug].astro     # 文章详情页（默认路由）
src/pages/[...permalink].astro      # 自定义 Permalink 路由
src/pages/archive.astro             # 归档页（标签/分类筛选）
src/pages/api/allPostMeta.json.ts   # 文章元数据 API

src/components/features/posts/
  - PostCard.astro                  # 文章卡片组件
  - PostMeta.astro                  # 文章元信息组件
  - PostNavigation.astro            # 上一篇/下一篇导航
  - RelatedPosts.astro              # 相关文章推荐
  - RandomPosts.astro               # 随机文章
  - ShareCard.astro                 # 分享卡片
  - atoms/EncryptedBadge.astro      # 加密标记

src/utils/content-utils.ts          # 文章获取、排序、相关推荐算法
src/utils/permalink-utils.ts        # Permalink 生成工具
```

### 8.2 外部库依赖

- **Astro**: 框架核心，Content Collection API
- **CryptoJS**: 客户端 AES 加密/解密
- **python-frontmatter**: 当前管理后台的 Frontmatter 解析
- **Pillow**: 当前管理后台的图片格式转换

---

## 9. 向后端迁移的影响点

### 9.1 前端需调整的部分

1. **数据获取方式**: 从 `getCollection("posts")` 改为 API 请求
2. **Content Collection Schema**: 若保留 Astro Content Layer，需配置远程数据源
3. **图片路径**: 确认后端返回的图片 URL 格式与当前相对路径兼容
4. **ID 类型**: 当前为文件路径字符串，后端可能使用 UUID 或数字 ID
5. **相邻文章关联**: 当前在构建时计算，后端需提供 API 或前端自行计算

### 9.2 保持不变的特性

1. **Frontmatter 字段结构**: 建议后端数据模型与当前 Schema 保持一致
2. **URL 路由策略**: Permalink、Alias、默认 Slug 逻辑可复用
3. **排序规则**: 置顶 > 优先级 > 发布日期的逻辑保持不变
4. **组件渲染逻辑**: PostCard、PostMeta 等组件数据格式一致时无需改动
5. **加密机制**: 前端加密/解密逻辑保持不变
