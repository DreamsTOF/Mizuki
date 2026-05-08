---
alwaysApply: false
description: 当创建新模块、定义 PO/Entity/DTO 对象或在四层架构间进行数据流转与转换时触发。
---
# 架构规范

## 四层架构 & 命名

| 层级 | 包路径 | 类后缀 |
|---|---|---|
| API | `api/controller`, `api/request`, `api/vo` | Controller, PageReq/CursorReq, VO |
| Application | `application/service`, `application/assembler` | AppService, Assembler |
| Domain | `domain/model/entity`, `domain/repository` | (无后缀), Repository, ErrorCode |
| Infrastructure | `infrastructure/persistence` | PO, Mapper, RepositoryImpl |

## API 路径

| 操作 | 方法 | 路径 |
|---|---|---|
| 创建 | POST | `/xxx/save` |
| 更新 | PUT | `/xxx/update` |
| 删除 | DELETE | `/xxx/remove/{id}` |
| 批量删除 | DELETE | `/xxx/removeByIds` |
| 详情 | GET | `/xxx/detail/{id}` |
| 列表 | GET | `/xxx/list` |
| 分页 | POST | `/xxx/page` |
| 游标 | POST | `/xxx/seek` |

## 禁止

| 禁止 | 替代 |
|---|---|
| Mapper 添加自定义方法 | 所有数据库操作在 RepositoryImpl 中完成 |
| 修改 PO 类 | PO 为最终入库类，业务方法写入 Entity |
