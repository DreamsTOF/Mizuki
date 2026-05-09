---
alwaysApply: false
description: 需要调用系统内置工具类时触发。用于快速索引现有能力，严禁重复造轮子。
---
# 工具速查
| 工具类 | 全限定路径 | 用途 |
|---|---|---|
| SmartValidator | `cn.dreamtof.core.utils.SmartValidator` | @Check 校验引擎 |
| Asserts | `cn.dreamtof.core.exception.Asserts` | 业务断言 |
| JsonUtils | `cn.dreamtof.core.utils.JsonUtils` | 唯一 ObjectMapper |
| DateUtils | `cn.dreamtof.core.utils.DateUtils` | 上海时区时间 |
| SmartTransactionTemplate | `cn.dreamtof.audit.utils.SmartTransactionTemplate` | 编程式事务 |
| FlexUltraInserter | `cn.dreamtof.audit.utils.FlexUltraInserter` | 批量插入 |
| SpringContextUtil | `cn.dreamtof.common.util.SpringContextUtil` | Bean 获取 |
## 禁止
| 禁止 | 替代 |
|---|---|
| `@Transactional` | `SmartTransactionTemplate.execute()` |
| `new ObjectMapper()` | `JsonUtils` |
| `LocalDateTime.now()` | `DateUtils.now()` |
| XML MyBatis 配置 | MyBatis-Flex 注解 |
| `new Thread()` | `VirtualTaskManager.execute()` |
| `Optional<T>` 返回值 | 返回 T 或 null |
| JOIN 连表 | 单表 IN 查询 + 内存合并 |
