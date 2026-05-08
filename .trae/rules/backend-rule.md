---
alwaysApply: false
description: 进行后端开发，用户指定查看后端规范时触发
---
# 后端开发守则

**技术栈**：JDK 25 / Spring Boot 3.5 / MyBatis-Flex / Sa-Token
**架构**：DDD 严格四层（api → application → domain → infrastructure）

## 工作流
1. 识别任务类型 → 2. 加载对应 catalogue/*.md → 3. 对照禁止项检查 → 4. 产出代码
5. 需求不明确时使用 `AskUserQuestion` 工具，禁止在文本中直接提问。

## 任务 → 规范索引

| 任务类型 | 规范文件 | 关键约束 |
|---|---|---|
| 查询/CRUD | catalogue/query.md | 禁止 JOIN，禁止链式 QueryWrapper |
| 架构/对象转换 | catalogue/architecture.md | 禁止 Mapper 加自定义方法 |
| 编码/校验/事务/异步/异常 | catalogue/code-conventions.md | 禁止 @Transactional，禁止 new Thread()，禁止魔法值，禁止手动构造 BaseResponse |
| 工具速查 | catalogue/tool.md | 禁止重复造轮子 |
