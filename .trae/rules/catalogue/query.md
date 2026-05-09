---
alwaysApply: false
description: 当涉及数据库 CRUD 操作、构造 QueryWrapper 条件查询、处理分页或游标查询时触发。
---
# 查询规范

## 请求类

- 分页请求 **必须** 继承 `cn.dreamtof.core.base.PageReq`，已有pageNum和pageSize字段
- 游标请求 **必须** 继承 `cn.dreamtof.core.base.CursorReq`，已有cursor和limit和next字段
- 若需要处理排序，**建议** 继承 `cn.dreamtof.query.base.SmartPageReq`，已内置完善的排序处理逻辑

## QueryWrapper

**逐步构造，禁止链式调用。**

## 查询契约

| 前缀            | 查不到时行为     | 返回值    |
| --------------- | ---------------- | --------- |
| `getByXxx`    | **抛异常** | T         |
| `findByXxx`   | 返回 null        | T 或 null |
| `listByXxx`   | 返回空列表       | List      |
| `pageByXxx`   | 返回空页         | Page      |
| `existsByXxx` | 返回 false       | boolean   |
| `countByXxx`  | 返回 0           | long      |

## 禁止

| 禁止                  | 替代                   |
| --------------------- | ---------------------- |
| JOIN 连表查询         | IN 批量查询 + 内存组装 |
| QueryWrapper 链式调用 | 逐步构造               |
