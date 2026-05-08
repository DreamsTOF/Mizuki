---
name: frontend-rule
description: Vue 3 + TypeScript 前端项目开发规范。当用户需要开发前端功能、创建组件、调用 API、编写样式、管理状态、处理路由权限时使用此规范。确保所有前端代码遵循统一的技术标准和最佳实践。
---

# frontend-rule

本规范定义了 Vue 3 + TypeScript 前端项目的开发标准，确保团队协作时代码风格一致、架构清晰。

## 技术栈
| 类别 | 技术选型 |
|------|----------|
| 框架 | Vue 3 + TypeScript |
| 构建工具 | Vite |
| 状态管理 | Pinia + VueUse + pinia-plugin-persistedstate |
| 路由 | Vue Router |
| UI 组件库 | Element Plus |
| 样式方案 | CSS 变量 + Scoped CSS |
| HTTP 客户端 | Axios |
| API 代码生成 | Orval + openapi2ts |
| Mock 服务 | Apifox Mock |

## 规范索引
| 场景 | 规范文件 | 核心内容 |
|------|----------|----------|
| 调用后端接口 | references/api.md | API架构、调用规范、错误处理 |
| 编写组件样式 | references/style.md | CSS变量、设计系统、样式规则 |
| 开发新组件 | references/component.md | 组件分层、命名、通信规范 |
| 管理全局状态 | references/state.md | Store设计、持久化规则 |
| 配置路由权限 | references/route.md | 动态路由、权限控制规范 |
| UI 交互设计 | references/ui.md | 消息提示、加载状态规范 |
| 代码风格规范 | references/code.md | 代码风格、提交规范 |

## 项目目录结构
```
src/
├── api/                    # API 相关
│   ├── generated/          # Orval 自动生成代码
│   ├── real/               # 业务使用的函数层
│   ├── api.ts              # Axios 实例配置
│   └── request-adapter.ts  # 请求适配器
├── components/             # 组件
│   ├── base/               # 基础组件
│   ├── common/             # 通用组件
│   ├── layout/             # 布局组件
│   └── {domain}/           # 业务领域组件
├── router/                 # 路由配置
├── stores/                 # Pinia Store
│   ├── domain/             # 领域 Store
│   └── workflow/           # 工作流 Store
├── styles/                 # 全局样式
│   ├── variables.css       # CSS 变量
│   └── global.css          # 全局样式
├── utils/                  # 工具函数
├── views/                  # 页面视图
├── App.vue                 # 根组件
└── main.ts                 # 入口文件
```

## 常用命令
| 命令 | 说明 |
|------|------|
| `pnpm dev` | 启动开发服务器 |
| `pnpm build` | 构建生产版本 |
| `pnpm lint` | 运行代码检查 |
| `pnpm api:gen` | 生成 API 代码 |
| `pnpm type-check` | 类型检查 |
