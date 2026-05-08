name: backend-rule
description: Spring Boot 3.5 + MyBatis-Flex 后端全量开发规范。涵盖架构、事务、查询、校验、异常、上下文及代码风格全量准则。Backend-Rule 全量开发规范 (Agent 专用版)这是本项目的唯一事实来源。在生成或修改代码前，必须完整阅读并严格遵守本规范的所有条款。🛑 核心禁令 (Critical Constraints) - 最高优先级作为 Agent，在执行任何任务前，必须自检是否违反以下禁令：事务禁止：禁止使用 @Transactional 注解。替代方案：必须使用 TransactionTemplate。事务开启：操作单张表时禁止开启事务；只有涉及多张表或多次写操作时才必须开启。查询禁止：禁止连表 (Join) 查询。替代方案：使用 IN 批量查询并在内存中手动组装。构造禁止：禁止 QueryWrapper 的链式调用。替代方案：必须分步逐步构造。配置禁止：禁止使用 XML 配置文件。替代方案：完全采用 MyBatis-Flex 的代码和注解方式。并发禁止：禁止使用 new Thread() 或直接调用 executor.execute()。替代方案：统一使用 VirtualTaskManager.execute() 以确保上下文自动传播。空值禁止：禁止使用 Optional<T> 作为返回值。替代方案：直接返回对象或 null。状态禁止：对于固定值字段（状态、类型等），禁止使用字符串/数字魔法值。替代方案：必须使用枚举 (Enum)。响应禁止：禁止手动构造失败的 BaseResponse。替代方案：必须使用 Asserts 工具类抛出业务异常。方法约定：getByXxx：查不到数据必须抛出异常。listByXxx：查不到数据必须返回空列表。pageByXxx：查不到数据必须返回空页。第一部分：架构规范 (Architecture)1. 技术栈Spring Boot 3.5 + JDK 25 / MyBatis-Flex / Sa-Token + JWT / Argon2 / MapStruct2. 四层架构API 层 (Controller/Request/VO) → Application 层 (AppService/Assembler) → Domain 层 (Entity/Repository/ErrorCode) → Infrastructure 层 (PO/Mapper/RepositoryImpl)
层级包路径职责APIapi/controller、api/request、api/vo接收请求、返回响应Applicationapplication/service、application/assembler业务流程编排、事务管理Domaindomain/model/entity、domain/repository领域模型、业务规则Infrastructureinfrastructure/persistence数据持久化3. 模块组织按业务领域划分模块：dreamtof-songs/
├── auth/           # 认证领域
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── song/           # 歌曲领域
├── log/            # 日志领域
4. 命名规范层级命名规则示例Domain Entity无后缀UserDomain Repository无后缀UserRepositoryDomain ErrorCodeErrorCode 后缀AuthErrorCodeInfrastructure POPO 后缀UserPOInfrastructure MapperMapper 后缀UserMapperInfrastructure RepositoryImplRepositoryImpl 后缀UserRepositoryImplApplication ServiceAppService 后缀UserAppServiceApplication AssemblerAssembler 后缀UserAssemblerAPI ControllerController 后缀UserControllerAPI RequestPageReq/CursorReq 后缀UserPageReqAPI VOVO 后缀LoginVO5. API 路径规范操作HTTP 方法路径创建POST/xxx/save更新PUT/xxx/update删除DELETE/xxx/remove/{id}批量删除DELETE/xxx/removeBatch详情查询GET/xxx/detail/{id}列表查询GET/xxx/list分页查询POST/xxx/page游标查询POST/xxx/seek第二部分：代码风格规范 (Code Style)1. 代码长度限制方法长度规则说明强制上限方法体不超过 120 行处理方式超过 120 行必须拆分为多个私有方法拆分原则每个方法保持单一职责参数数量规则说明强制上限方法参数不超过 4 个处理方式超过 4 个参数必须封装为对象封装位置使用 Request DTO 或专门的参数对象2. 注释规范必须注释的场景场景注释类型说明公共 API 方法Javadoc说明方法用途、参数、返回值、异常复杂业务逻辑行内注释解释业务规则和决策原因关键算法块注释说明算法思路和关键步骤边界条件行内注释说明特殊处理的原因Javadoc 格式/**
 * 根据用户名查询用户
 *
 * @param username 用户名，不能为空
 * @return 用户实体
 * @throws BusinessException 用户不存在时抛出异常
 */
public User getByUsername(String username) {
    Asserts.notBlank(username, "用户名不能为空");
    UserPO po = repository.getByUsername(username);
    Asserts.notNull(po, "用户不存在");
    return assembler.toEntity(po);
}
复杂逻辑注释public void processOrder(Order order) {
    // 业务规则：订单金额超过1000元需要人工审核
    if (order.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
        order.setStatus(OrderStatus.PENDING_REVIEW);
    }
    
    // 边界条件：库存不足时回滚并通知用户
    if (!inventoryService.checkStock(order.getProductId(), order.getQuantity())) {
        throw new BusinessException(OrderErrorCode.INSUFFICIENT_STOCK);
    }
}
3. null 值处理规则说明使用 Asserts业务规则校验使用 Asserts 工具类禁止 Optional不使用 Optional<T> 包装返回值减少链式调用避免过长的链式调用，拆分为多步4. 异常消息与枚举强制使用规范核心原则：对于只有少数几个固定值的字段（如状态、类型、类别等），必须使用枚举，禁止使用字符串魔法值。场景必须使用枚举示例用户状态✅ 必须UserStatus.ACTIVE 而非 "ACTIVE"订单状态✅ 必须OrderStatus.PENDING 而非 "PENDING"性别✅ 必须Gender.MALE 而非 "MALE"角色类型✅ 必须RoleType.ADMIN 而非 "ADMIN"支付状态✅ 必须PaymentStatus.SUCCESS 而非 "SUCCESS"日志级别✅ 必须LogLevel.INFO 而非 "INFO"布尔标志❌ 使用 booleanisActive 而非 "1"/"0"无限制文本❌ 使用 String用户名、描述等禁止行为：❌ 禁止使用字符串字面量表示状态："ACTIVE"、"PENDING"、"DELETED"❌ 禁止使用数字常量表示状态：0、1、2❌ 禁止在代码中进行字符串比较："ACTIVE".equals(status)✅ 必须使用枚举类型：UserStatus status枚举位置：使用范围建议位置仅在单个模块使用模块的 domain/model/enums/ 目录跨模块使用公共模块的 common/enums/ 目录仅在单个类使用作为内部枚举第三部分：查询风范规范 (Query)1. QueryWrapper 构造逐步构造，禁止链式：QueryWrapper queryWrapper = QueryWrapper.create();

if (StringUtils.isNotBlank(req.getUsername())) {
    queryWrapper.and(w -> w.where(USER_PO.USERNAME.like(req.getUsername())));
}

if (req.getStatus() != null) {
    queryWrapper.and(w -> w.where(USER_PO.STATUS.eq(req.getStatus())));
}
2. 避免 N+1 问题禁止连表查询，使用 IN 批量查询：// 收集关联 ID
List<UUID> userIds = orders.stream().map(OrderPO::getUserId).distinct().toList();

// 批量查询
List<UserPO> users = userRepository.listByIds(userIds);
Map<UUID, UserPO> userMap = users.stream()
        .collect(Collectors.toMap(UserPO::getId, Function.identity()));
3. 分页查询Page<UserPO> page = Page.of(req.getPageNum(), req.getPageSize());
return mapper.paginate(page, queryWrapper);
4. 游标查询if (lastId != null) {
    queryWrapper.where(USER_PO.ID.gt(lastId));
}
queryWrapper.orderBy(USER_PO.ID, true).limit(limit);
第四部分：事务管理规范 (Transaction)1. 核心原则与开启条件禁止 @Transactional 注解，统一使用 TransactionTemplate。场景是否开启事务操作多张表必须开启操作单张表禁止开启2. 写事务public String createOrder(CreateOrderReq request) {
    return transactionTemplate.execute(status -> {
        orderRepository.create(order);
        inventoryRepository.update(inventory);
        return order.getId().toString();
    });
}
3. 只读事务public List<OrderPO> listOrders(String userId) {
    return readOnlyTransactionTemplate.execute(status -> {
        return orderRepository.listByUserId(userId);
    });
}
4. 单表操作（不开启事务）public User getById(UUID id) {
    return userRepository.getById(id);
}
第五部分：参数校验规范 (Validation)1. @Check 注解项目自定义注解，用于 Request 类字段校验。属性类型默认值说明msgString"参数"业务错误提示消息requiredbooleanfalse是否必填（深度判空）notNullbooleanfalse仅校验非 nullminlongLong.MIN_VALUE最小长度/最小值maxlongLong.MAX_VALUE最大长度/最大值positivebooleanfalse必须为正数noNullsbooleanfalse集合内不能包含 nullregexString""自定义正则表达式typeString""内置格式：mobile/email/idcard/numericvalidbooleanfalse是否递归校验嵌套对象使用示例：@Data
public class CreateUserReq {

    @Check(msg = "用户名", required = true, min = 2, max = 20)
    private String username;

    @Check(msg = "密码", required = true, min = 6, max = 32)
    private String password;

    @Check(msg = "邮箱", type = "email")
    private String email;

    @Check(msg = "手机号", type = "mobile")
    private String mobile;

    @Check(msg = "年龄", positive = true, max = 150)
    private Integer age;

    @Check(msg = "角色列表", required = true, noNulls = true)
    private List<String> roles;

    @Check(msg = "扩展信息", valid = true)
    private UserExtInfo extInfo;
}
2. SmartValidator在 Service 层调用，自动执行 @Check 注解的校验逻辑：@Service
@RequiredArgsConstructor
public class UserAppService {

    public void createUser(CreateUserReq request) {
        SmartValidator.validate(request);
        // 业务逻辑...
    }
}
第六部分：异常处理规范 (Exception)1. Asserts 工具类统一使用 Asserts 进行业务校验，抛出 BusinessException。方法说明Asserts.notNull(obj, "消息")非空校验Asserts.notBlank(str, "消息")非空白校验Asserts.notEmpty(list, "消息")集合非空校验Asserts.isTrue(condition, "消息")条件校验Asserts.fail(errorCode, args...)直接抛异常使用示例：// 非空校验
Asserts.notNull(user, "用户不存在");

// 条件校验
Asserts.isTrue(order.getAmount().compareTo(BigDecimal.ZERO) > 0, "订单金额必须大于0");

// 直接抛异常
Asserts.fail(OrderErrorCode.INSUFFICIENT_STOCK, productId, quantity);
2. 错误码范围范围分类0成功10000-19999基础错误20000-29999认证错误30000-39999业务错误40000-49999资源错误第七部分：返回值规范 (Response)1. BaseResponse 结构所有接口统一返回 BaseResponse<T>：public class BaseResponse<T> {
    private boolean success;
    private int code;
    private T data;
    private String message;
    private long timestamp;
}
2. ResultUtils 工具类统一使用 ResultUtils 构造成功响应：@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserAppService appService;

    @PostMapping("save")
    public BaseResponse<User> save(@RequestBody User entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<User> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<User>> list() {
        return ResultUtils.success(appService.list());
    }

    @PostMapping("page")
    public BaseResponse<Page<User>> page(@RequestBody UserPageReq pageRequest) {
        return ResultUtils.success(appService.page(pageRequest));
    }
}
3. 失败响应失败响应统一抛出异常，不要手动构造：// 正确
Asserts.notNull(user, "用户不存在");

// 错误
return new BaseResponse<>(10000, null, "用户不存在", false);
第八部分：上下文管理规范 (Context)1. 核心原则所有异步任务统一使用 VirtualTaskManager，自动携带上下文。2. VirtualTaskManager 使用// 提交异步任务
VirtualTaskManager.execute(() -> {
    // 上下文自动传播
    UUID traceId = OperationContext.traceId();
    Operator operator = OperationContext.get().getOperator();
});

// 异步获取结果
public CompletableFuture<Result> asyncProcess(Data data) {
    return VirtualTaskManager.supply(() -> doProcess(data));
}
3. 获取上下文信息// 获取当前操作人
Operator operator = OperationContext.get().getOperator();

// 获取追踪 ID
UUID traceId = OperationContext.traceId();

// 判断请求类型
if (OperationContext.isReal()) {
    // 真实 HTTP 请求
}
4. 禁止事项禁止替代方案new Thread()VirtualTaskManager.execute()直接调用 executor.execute()配置 ContextPropagator在构造函数中获取 OperationContext在方法执行时获取第九部分：工具速查 (Tools Quick Reference)1. 核心工具类工具类全限定路径作用SmartValidatorcn.dreamtof.core.utils.SmartValidator参数校验引擎（基于 @Check 注解）Assertscn.dreamtof.core.exception.Asserts业务规则断言（非空、空集合、条件校验）FastBeanMetacn.dreamtof.core.utils.FastBeanMeta基于 LambdaMetafactory 的零损耗元数据访问器FastReflectUtilscn.dreamtof.core.utils.FastReflectUtils高性能反射工具（属性读写、浅拷贝、深拷贝）JsonUtilscn.dreamtof.core.utils.JsonUtils全局唯一的Json序列化（Asia/Shanghai时区）DateUtilscn.dreamtof.core.utils.DateUtils统一时间工具（Asia/Shanghai 时区）CacheKeyUtilscn.dreamtof.core.utils.CacheKeyUtils对象 → JSON → MD5 缓存键生成SmartTreeBuildercn.dreamtof.core.utils.SmartTreeBuilderO(N) 树形结构构建器（链式调用）TraceIdHandlercn.dreamtof.core.utils.TraceIdHandlerUUIDv7 链路追踪 ID 生成SmartTransactionTemplatecn.dreamtof.audit.utils.SmartTransactionTemplate智能事务模板（替代 @Transactional）FlexUltraInsertercn.dreamtof.audit.utils.FlexUltraInserter自适应批量注入器（2000条/批，异常降级）QueryHelpercn.dreamtof.audit.utils.QueryHelper批量查询助手（500条/批，空集合拦截）UniversalJsonTypeHandlercn.dreamtof.common.persistence.handler.UniversalJsonTypeHandlerJSON 字段类型处理器（自动适配 PG/jsonb 与 MySQL/JSON）SaTokenMixUtilcn.dreamtof.common.util.SaTokenMixUtil双 Token 模式（AT 30min + RT 7d，无感刷新）SpringContextUtilcn.dreamtof.common.util.SpringContextUtilSpring 上下文工具（静态获取 Bean）ExportUtilcn.dreamtof.common.util.excel.ExportUtil动态 Excel 导出（支持多级表头、List 字段展开）ExcelDynamicImportUtilcn.dreamtof.common.util.excel.ExcelDynamicImportUtil智能 Excel 导入（合并单元格自动填充）ExcelDictcn.dreamtof.common.util.excel.ExcelDictExcel 字典映射注解（数字→文字转换）TokenPayloadcn.dreamtof.common.util.TokenPayload双 Token 模式载荷（userId + createTime）2. ⚡ 核心依赖FastBeanMeta / FastReflectUtils: caffeine + lambda-metafactory (JVM 内置)JsonUtils: jackson-databind + jackson-datatype-jsr310ExportUtil / ExcelDynamicImportUtil: alibaba-easyexcel + hutoolSaTokenMixUtil: sa-tokenFlexUltraInserter: mybatis-flex