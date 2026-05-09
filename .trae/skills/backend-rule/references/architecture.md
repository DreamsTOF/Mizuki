# 架构规范

## 四层架构

```
API 层 (Controller/Request/VO) → Application 层 (AppService/Assembler) → Domain 层 (Entity/Repository/ErrorCode) → Infrastructure 层 (PO/Mapper/RepositoryImpl)
```

| 层级             | 包路径                                           | 职责          |
| -------------- | --------------------------------------------- | ----------- |
| API            | `api/controller`、`api/request`、`api/vo`       | 接收请求、返回响应   |
| Application    | `application/service`、`application/assembler` | 业务流程编排、事务管理 |
| Domain         | `domain/model/entity`、`domain/repository`     | 领域模型、业务规则   |
| Infrastructure | `infrastructure/persistence`                  | 数据持久化       |

## 模块组织

按业务领域划分模块：

```
dreamtof-songs/
├── auth/           # 认证领域
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── song/           # 歌曲领域
├── log/            # 日志领域
```

## 命名规范

| 层级                            | 命名规则                 | 示例                   |
| ----------------------------- | -------------------- | -------------------- |
| Domain Entity                 | 无后缀                  | `User`               |
| Domain Repository             | 无后缀                  | `UserRepository`     |
| Domain ErrorCode              | ErrorCode 后缀         | `AuthErrorCode`      |
| Infrastructure PO             | PO 后缀                | `UserPO`             |
| Infrastructure Mapper         | Mapper 后缀            | `UserMapper`         |
| Infrastructure RepositoryImpl | RepositoryImpl 后缀    | `UserRepositoryImpl` |
| Application Service           | AppService 后缀        | `UserAppService`     |
| Application Assembler         | Assembler 后缀         | `UserAssembler`      |
| API Controller                | Controller 后缀        | `UserController`     |
| API Request                   | PageReq/CursorReq 后缀 | `UserPageReq`        |
| API VO                        | VO 后缀                | `LoginVO`            |

## API 路径规范

| 操作   | HTTP 方法 | 路径                 |
| ---- | ------- | ------------------ |
| 创建   | POST    | `/xxx/save`        |
| 更新   | PUT     | `/xxx/update`      |
| 删除   | DELETE  | `/xxx/remove/{id}` |
| 批量删除 | DELETE  | `/xxx/removeBatch` |
| 详情查询 | GET     | `/xxx/detail/{id}` |
| 列表查询 | GET     | `/xxx/list`        |
| 分页查询 | POST    | `/xxx/page`        |
| 游标查询 | POST    | `/xxx/seek`        |

---

## ❌ 绝对禁止 (Critical Constraints)

 **禁止为 Mapper 添加自定义方法** → 所有数据库相关的操作必须在 `RepositoryImpl` 中完成。
 **禁止修改 PO 类** → 以 PO 类为最终入库类，业务方法写入 Entity 类中。

#
