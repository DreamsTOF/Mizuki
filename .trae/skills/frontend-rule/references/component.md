# 组件开发规范

## 组件分层
| 层级 | 目录 | 说明 |
|------|------|------|
| 基础组件 | `src/components/base/` | 通用UI组件，全项目复用 |
| 通用业务组件 | `src/components/{domain}/` | 特定业务领域通用组件 |
| 页面专属组件 | `src/views/{page}/components/` | 仅在当前页面使用的组件 |

## 命名规范
- 文件名：PascalCase，如`SongCard.vue`
- 组件名：PascalCase，与文件名保持一致
- 组件引用：使用PascalCase

## 状态管理策略
| 组件层级 | 状态管理方式 |
|----------|--------------|
| 底层基础组件 | useVModel模式，支持外部驱动+自动读取Store |
| 中层业务组件 | Props + Emit 单向数据流 |
| 顶层页面组件 | Pinia Store 全局状态 |

## 组件通信规则
| 层级距离 | 通信方式 |
|----------|----------|
| 1-2层父子组件 | Props + Emit |
| 功能块内部深层组件 | Provide/Inject |
| 远距离/全局通信 | Pinia Store |

## Element Plus使用规则
- 基础组件（Button、Input等）直接使用
- 复杂交互组件（Message、Confirm等）必须使用二次封装后的版本
- 统一使用已封装的Notify、useConfirm、BaseEmpty等组件
