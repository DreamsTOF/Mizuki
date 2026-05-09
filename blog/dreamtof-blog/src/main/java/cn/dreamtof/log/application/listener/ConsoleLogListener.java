//package cn.dreamtof.log.application.listener;
//
//import cn.dreamtof.core.constants.GlobalConstants;
//import cn.dreamtof.core.utils.JsonUtils;
//import cn.dreamtof.log.config.LogProperties;
//import cn.dreamtof.log.core.LogEvent;
//import cn.dreamtof.log.core.LogLevel;
//import cn.dreamtof.log.core.LogMarkers;
//import cn.dreamtof.log.spi.LogListener;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.slf4j.Marker;
//import org.slf4j.helpers.MessageFormatter;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.stereotype.Component;
//
//import java.io.StringWriter;
//import java.time.Instant;
//
//@Component
//@ConditionalOnProperty(prefix = "dreamtof.log", name = "console-enabled", havingValue = "true", matchIfMissing = true)
//public class ConsoleLogListener implements LogListener {
//
//    // 🎨 ANSI 配色方案：用于控制台高亮，排查问题更直观
//    private static final String RESET = "\u001B[0m";
//    private static final String RED = "\u001B[31m";
//    private static final String GREEN = "\u001B[32m";
//    private static final String YELLOW = "\u001B[33m";
//    private static final String BLUE = "\u001B[34m";
//    private static final String CYAN = "\u001B[36m";
//    private static final String PURPLE = "\u001B[35m";
//
//    private final LogProperties properties;
//
//    public ConsoleLogListener(LogProperties properties) {
//        this.properties = properties;
//    }
//
//    @Override
//    public void onLog(LogEvent event) {
//        // 1. 拦截开关：如果配置中关闭了控制台输出，则直接丢弃，不消耗计算资源
//        if (!properties.isConsoleEnabled()) {
//            return;
//        }
//
//        // 2. 延迟格式化：执行 SLF4J 风格的占位符替换 (消耗 CPU 的操作放在这里)
//        String formattedMsg = MessageFormatter.arrayFormat(event.getMessagePattern(), event.getArgs()).getMessage();
//
//        // 3. 格式路由：根据配置选择输出格式 (纯文本 vs JSON)
//        String output = "json".equalsIgnoreCase(properties.getOutput()) ?
//                buildJson(event, formattedMsg) : buildPrettyText(event, formattedMsg);
//
//        // 4. 物理写入：输出到控制台
//        System.out.println(output);
//    }
//
//    /**
//     * 🧱 构建高亮纯文本格式 (为开发者调试优化)
//     */
//    private String buildPrettyText(LogEvent event, String msg) {
//        StringBuilder sb = new StringBuilder();
//        String levelColor = getLevelColor(event.getLevel(), event.getMarker());
//
//        // 1. 时间与级别
//        sb.append(CYAN).append(GlobalConstants.DATETIME_MS_FORMATTER.format(Instant.ofEpochMilli(event.getTimestamp()))).append(" ");
//        sb.append(levelColor).append(String.format("%-8s", event.getLevel().getLabel())).append(RESET).append(" ");
//
//        // 2. 标记 (Marker)
//        if (event.getMarker() != null) {
//            sb.append(PURPLE).append("[").append(event.getMarker().getName()).append("]").append(RESET).append(" ");
//        }
//
//        // 3. 链路追踪 (TraceId)
//        if (event.getContext() != null && event.getContext().getTraceId() != null) {
//            // 注意：UUID 必须调用 toString()
//            sb.append(BLUE).append("[").append(event.getContext().getTraceId().toString()).append("]").append(RESET).append(" ");
//        }
//
//        // 4. 现场线程名 (绿色高亮，排查并发与异步任务的神器)
//        String threadInfo = event.getThreadName() != null ? event.getThreadName() : "Unknown";
//        sb.append(GREEN).append("[").append(threadInfo).append("]").append(RESET).append(" ");
//
//        // 5. Logger 名称 (缩写) 与 正文
//        sb.append(YELLOW).append(shortenName(event.getLoggerName())).append(RESET).append(" : ").append(msg);
//
//        // 6. 异常堆栈 (如果有)
//        if (event.getThrowable() != null) {
//            sb.append("\n").append(getStackTrace(event.getThrowable()));
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * 🧱 构建 JSON 格式 (为日志收集系统 ELK/Loki 优化)
//     */
//    private String buildJson(LogEvent event, String msg) {
//        try {
//            ObjectNode root = JsonUtils.createObjectNode();
//
//            // 基础信息
//            root.put("time", GlobalConstants.DATETIME_MS_FORMATTER.format(Instant.ofEpochMilli(event.getTimestamp())));
//            root.put("level", event.getLevel().getLabel());
//            if (event.getMarker() != null) {
//                root.put("marker", event.getMarker().getName());
//            }
//
//            // 定位信息
//            root.put("logger", event.getLoggerName());
//            root.put("thread", event.getThreadName());
//            root.put("msg", msg);
//
//            // 链路与上下文信息 (完整适配 UUID)
//            if (event.getContext() != null) {
//                if (event.getContext().getTraceId() != null) {
//                    root.put("trace_id", event.getContext().getTraceId().toString());
//                }
//                if (event.getContext().getSpanId() != null) {
//                    root.put("span_id", event.getContext().getSpanId().toString());
//                }
//                if (event.getContext().getParentSpanId() != null) {
//                    root.put("parent_span_id", event.getContext().getParentSpanId().toString());
//                }
//
//                // 业务操作人信息
//                if (event.getOperator() != null) {
//                    root.set("user", JsonUtils.MAPPER.valueToTree(event.getOperator()));
//                }
//            }
//
//            // 异常兜底
//            if (event.getThrowable() != null) {
//                root.put("stack", getStackTrace(event.getThrowable()));
//            }
//
//            return JsonUtils.toJsonString(root);
//        } catch (Exception e) {
//            // 序列化失败时的极端兜底防线
//            return "{\"level\":\"ERROR\",\"msg\":\"Log serialization failed: " + e.getMessage() + "\"}";
//        }
//    }
//
//    /**
//     * 提取完整的异常堆栈信息为字符串
//     */
//    private String getStackTrace(Throwable t) {
//        StringWriter sw = new StringWriter();
//        t.printStackTrace(new java.io.PrintWriter(sw));
//        return sw.toString();
//    }
//
//    /**
//     * 智能获取日志级别对应的颜色
//     */
//    private String getLevelColor(LogLevel level, Marker marker) {
//        // FATAL 级别或带有 FATAL Marker 的给予紫色高亮，引起极度重视
//        if (marker == LogMarkers.FATAL) return PURPLE;
//
//        return switch (level) {
//            case ERROR -> RED;
//            case WARN -> YELLOW;
//            case INFO -> GREEN;
//            default -> BLUE; // DEBUG, TRACE
//        };
//    }
//
//    /**
//     * 缩写 Logger 名称，例如：cn.dreamtof.service.UserService -> UserService
//     */
//    private String shortenName(String name) {
//        if (name == null) return "Unknown";
//        int idx = name.lastIndexOf(".");
//        return idx != -1 ? name.substring(idx + 1) : name;
//    }
//}