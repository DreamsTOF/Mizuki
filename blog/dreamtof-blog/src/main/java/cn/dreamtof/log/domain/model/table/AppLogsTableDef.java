package cn.dreamtof.log.domain.model.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 *  表定义层。
 *
 * @author dream
 */
public class AppLogsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public static final AppLogsTableDef APP_LOGS = new AppLogsTableDef();

    /**
     * 运行环境 (如: prod, test, dev)
     */
    public final QueryColumn ENV = new QueryColumn(this, "env");

    /**
     * 日志级别 (INFO, WARN, ERROR 等)
     */
    public final QueryColumn LEVEL = new QueryColumn(this, "level");

    /**
     * 日志标记路由 (AUDIT, SECURITY 等)
     */
    public final QueryColumn MARKER = new QueryColumn(this, "marker");

    /**
     * 当前节点标识 (UUIDv7)
     */
    public final QueryColumn SPAN_ID = new QueryColumn(this, "span_id");

    /**
     * 操作人ID/用户ID
     */
    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    /**
     * 应用/微服务名称 (如: order-service)
     */
    public final QueryColumn APP_NAME = new QueryColumn(this, "app_name");

    /**
     * 格式化后的日志正文
     */
    public final QueryColumn MESSAGE = new QueryColumn(this, "message");

    /**
     * 全局链路标识 (UUIDv7)
     */
    public final QueryColumn TRACE_ID = new QueryColumn(this, "trace_id");

    /**
     * 客户端来源IP
     */
    public final QueryColumn CLIENT_IP = new QueryColumn(this, "client_ip");

    /**
     * 租户ID/企业ID
     */
    public final QueryColumn TENANT_ID = new QueryColumn(this, "tenant_id");

    /**
     * 日志物理发生时间戳 (毫秒精度)
     */
    public final QueryColumn TIMESTAMP = new QueryColumn(this, "timestamp");

    /**
     * 动态上下文 JSON (如设备型号、浏览器UA等未独立建列的属性)
     */
    public final QueryColumn EXT_CONTEXT = new QueryColumn(this, "ext_context");

    /**
     * 记录器名称 (类全限定名)
     */
    public final QueryColumn LOGGER_NAME = new QueryColumn(this, "logger_name");

    /**
     * 异常堆栈信息 (仅异常时存在)
     */
    public final QueryColumn STACK_TRACE = new QueryColumn(this, "stack_trace");

    /**
     * 物理发生线程名称
     */
    public final QueryColumn THREAD_NAME = new QueryColumn(this, "thread_name");

    /**
     * 父节点标识 (如果是 HTTP 入口/根节点则为 NULL)
     */
    public final QueryColumn PARENT_SPAN_ID = new QueryColumn(this, "parent_span_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{TIMESTAMP, APP_NAME, ENV, LEVEL, MARKER, LOGGER_NAME, THREAD_NAME, TRACE_ID, SPAN_ID, PARENT_SPAN_ID, USER_ID, TENANT_ID, CLIENT_IP, MESSAGE, STACK_TRACE, EXT_CONTEXT};

    public AppLogsTableDef() {
        super("", "app_logs");
    }

    private AppLogsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AppLogsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AppLogsTableDef("", "app_logs", alias));
    }

}
