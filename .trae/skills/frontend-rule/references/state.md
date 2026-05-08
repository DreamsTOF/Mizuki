# 状态管理规范

## Store分类
| 类型 | 职责 | 存储位置 | 示例 |
|------|------|----------|------|
| Domain Store | 核心业务数据，全局共享 | domain/目录，持久化 | userStore、songStore |
| Workflow Store | 临时UI状态，特定功能使用 | workflow/目录，不持久化 | editorStore、wizardStore |

## 编写规则
1. 统一使用Composition API风格编写Pinia Store
2. 状态、计算属性、方法明确分离
3. 异步逻辑封装在Store内部处理
4. 所有状态和方法必须提供TypeScript类型定义

## 特殊功能
- 状态快照：使用VueUse的`useRefHistory`实现撤销/重做功能
- 状态持久化：使用`pinia-plugin-persistedstate`实现需要持久化的状态
- 单一职责：每个Store只负责一个业务领域，避免相互依赖
