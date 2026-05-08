package cn.dreamtof.log.domain.model;

import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;

import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.*;


/**
 *  实体类
 *
 * @author dream
 * @since 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "app_logs",dataSource = "clickhouse")
@Schema(name="app_logs",description = "")
public class AppLogs implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志物理发生时间戳 (毫秒精度)
     */
    @Schema(description = "日志物理发生时间戳 (毫秒精度)")
    private LocalDateTime timestamp;
    /**
     * 应用/微服务名称 (如: order-service)
     */
    @Schema(description = "应用/微服务名称 (如: order-service)")
    private String appName;
    /**
     * 运行环境 (如: prod, test, dev)
     */
    @Column(value = "env")
    @Schema(description = "运行环境 (如: prod, test, dev)")
    private String env;
    /**
     * 日志级别 (INFO, WARN, ERROR 等)
     */
    @Schema(description = "日志级别 (INFO, WARN, ERROR 等)")
    private String level;
    /**
     * 日志标记路由 (AUDIT, SECURITY 等)
     */
    @Column(value = "marker")
    @Schema(description = "日志标记路由 (AUDIT, SECURITY 等)")
    private String marker;
    /**
     * 记录器名称 (类全限定名)
     */
    @Column(value = "logger_name")
    @Schema(description = "记录器名称 (类全限定名)")
    private String loggerName;
    /**
     * 物理发生线程名称
     */
    @Column(value = "thread_name")
    @Schema(description = "物理发生线程名称")
    private String threadName;
    /**
     * 全局链路标识 (UUIDv7)
     */
    @Schema(description = "全局链路标识 (UUIDv7)")
    private UUID traceId;
    /**
     * 当前节点标识 (UUIDv7)
     */
    @Column(value = "span_id")
    @Schema(description = "当前节点标识 (UUIDv7)")
    private UUID spanId;
    /**
     * 父节点标识 (如果是 HTTP 入口/根节点则为 NULL)
     */
    @Column(value = "parent_span_id")
    @Schema(description = "父节点标识 (如果是 HTTP 入口/根节点则为 NULL)")
    private UUID parentSpanId;
    /**
     * 操作人ID/用户ID
     */
    @Column(value = "user_id")
    @Schema(description = "操作人ID/用户ID")
    private String userId;
    /**
     * 租户ID/企业ID
     */
    @Column(value = "tenant_id")
    @Schema(description = "租户ID/企业ID")
    private String tenantId;
    /**
     * 客户端来源IP
     */
    @Column(value = "client_ip")
    @Schema(description = "客户端来源IP")
    private String clientIp;
    /**
     * 格式化后的日志正文
     */
    @Column(value = "message")
    @Schema(description = "格式化后的日志正文")
    private String message;
    /**
     * 异常堆栈信息 (仅异常时存在)
     */
    @Column(value = "stack_trace")
    @Schema(description = "异常堆栈信息 (仅异常时存在)")
    private String stackTrace;
    /**
     * 动态上下文 JSON (如设备型号、浏览器UA等未独立建列的属性)
     */
    @Column(value = "ext_context")
    @Schema(description = "动态上下文 JSON (如设备型号、浏览器UA等未独立建列的属性)")
    private String extContext;

    /** 审计显示: 日志物理发生时间戳 (毫秒精度) */
    @Schema(description = "审计显示: 日志物理发生时间戳 (毫秒精度)")
    public static final String SHOW_TIMESTAMP = "timestamp";

    /** 审计显示: 应用/微服务名称 (如: order-service) */
    @Schema(description = "审计显示: 应用/微服务名称 (如: order-service)")
    public static final String SHOW_APPNAME = "appName";

    /** 审计显示: 运行环境 (如: prod, test, dev) */
    @Schema(description = "审计显示: 运行环境 (如: prod, test, dev)")
    public static final String SHOW_ENV = "env";

    /** 审计显示: 日志级别 (INFO, WARN, ERROR 等) */
    @Schema(description = "审计显示: 日志级别 (INFO, WARN, ERROR 等)")
    public static final String SHOW_LEVEL = "level";

    /** 审计显示: 日志标记路由 (AUDIT, SECURITY 等) */
    @Schema(description = "审计显示: 日志标记路由 (AUDIT, SECURITY 等)")
    public static final String SHOW_MARKER = "marker";

    /** 审计显示: 记录器名称 (类全限定名) */
    @Schema(description = "审计显示: 记录器名称 (类全限定名)")
    public static final String SHOW_LOGGERNAME = "loggerName";

    /** 审计显示: 物理发生线程名称 */
    @Schema(description = "审计显示: 物理发生线程名称")
    public static final String SHOW_THREADNAME = "threadName";

    /** 审计显示: 全局链路标识 (UUIDv7) */
    @Schema(description = "审计显示: 全局链路标识 (UUIDv7)")
    public static final String SHOW_TRACEID = "traceId";

    /** 审计显示: 当前节点标识 (UUIDv7) */
    @Schema(description = "审计显示: 当前节点标识 (UUIDv7)")
    public static final String SHOW_SPANID = "spanId";

    /** 审计显示: 父节点标识 (如果是 HTTP 入口/根节点则为 NULL) */
    @Schema(description = "审计显示: 父节点标识 (如果是 HTTP 入口/根节点则为 NULL)")
    public static final String SHOW_PARENTSPANID = "parentSpanId";

    /** 审计显示: 操作人ID/用户ID */
    @Schema(description = "审计显示: 操作人ID/用户ID")
    public static final String SHOW_USERID = "userId";

    /** 审计显示: 租户ID/企业ID */
    @Schema(description = "审计显示: 租户ID/企业ID")
    public static final String SHOW_TENANTID = "tenantId";

    /** 审计显示: 客户端来源IP */
    @Schema(description = "审计显示: 客户端来源IP")
    public static final String SHOW_CLIENTIP = "clientIp";

    /** 审计显示: 格式化后的日志正文 */
    @Schema(description = "审计显示: 格式化后的日志正文")
    public static final String SHOW_MESSAGE = "message";

    /** 审计显示: 异常堆栈信息 (仅异常时存在) */
    @Schema(description = "审计显示: 异常堆栈信息 (仅异常时存在)")
    public static final String SHOW_STACKTRACE = "stackTrace";

    /** 审计显示: 动态上下文 JSON (如设备型号、浏览器UA等未独立建列的属性) */
    @Schema(description = "审计显示: 动态上下文 JSON (如设备型号、浏览器UA等未独立建列的属性)")
    public static final String SHOW_EXTCONTEXT = "extContext";

}
