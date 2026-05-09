# 代码规范

## 异步处理
- 统一使用async/await语法，禁止.then()链式调用
- 使用try-catch捕获异常

## 注释规则
- 仅对复杂逻辑、特殊处理添加注释，注释说明"为什么"而非"是什么"
- 代码本身要具备可读性，通过清晰命名减少不必要注释

## Git提交规范
- 格式：`[#Issue号] 简洁描述`
- 示例：`[#123] 添加歌曲列表分页功能`

## 提交检查
提交代码前必须通过以下检查：
1. TypeScript类型检查：`pnpm type-check`
2. ESLint检查：`pnpm lint`
3. 格式化检查：`pnpm format`

## 命名规范
| 类型 | 命名方式 | 示例 |
|------|----------|------|
| 变量/函数 | camelCase | `songList`, `fetchData` |
| 常量 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| 类/接口/组件 | PascalCase | `UserService`, `SongCard.vue` |
| 工具/类型文件 | camelCase | `formatDate.ts` |

## 导入顺序
1. Vue相关导入
2. 第三方库导入
3. 项目内部模块导入
4. 类型导入
5. 样式导入

## 性能优化要求
- 路由统一使用懒加载
- 长列表使用虚拟滚动
- 图片使用懒加载，合理选择尺寸和格式
- 合理使用computed、watch，避免不必要计算
