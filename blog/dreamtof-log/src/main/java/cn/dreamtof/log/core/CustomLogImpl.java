package cn.dreamtof.log.core;

import cn.dreamtof.core.context.OperationContext;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;

import java.io.Serial;

/**
 * 完整的 SLF4J 日志桥接器实现
 */
public class CustomLogImpl extends LegacyAbstractLogger {

    @Serial
    private static final long serialVersionUID = 1L;
    private final String name;

    public CustomLogImpl(String name) {
        this.name = name;
    }

    // --- 私有辅助方法，用于判断级别（绝对不能加 @Override） ---
    private boolean checkLevel(int levelInt) {
        // 动态获取最新配置，确保配置变更即时生效
        return levelInt >= LogWriter.getResolvedLevel(this.name).getWeight();
    }

    // --- 实现 AbstractLogger 要求的核心级别判断 ---
    @Override public boolean isTraceEnabled() { return checkLevel(0); }
    @Override public boolean isDebugEnabled() { return checkLevel(10); }
    @Override public boolean isInfoEnabled() { return checkLevel(20); }
    @Override public boolean isWarnEnabled() { return checkLevel(30); }
    @Override public boolean isErrorEnabled() { return checkLevel(40); }

    // --- 实现 LegacyAbstractLogger 中的 Marker 重载 (委托给无参方法) ---
    @Override public boolean isTraceEnabled(Marker marker) { return isTraceEnabled(); }
    @Override public boolean isDebugEnabled(Marker marker) { return isDebugEnabled(); }
    @Override public boolean isInfoEnabled(Marker marker) { return isInfoEnabled(); }
    @Override public boolean isWarnEnabled(Marker marker) { return isWarnEnabled(); }
    @Override public boolean isErrorEnabled(Marker marker) { return isErrorEnabled(); }

    // --- 实现 AbstractLogger 的核心抽象方法 ---

    @Override
    protected String getFullyQualifiedCallerName() {
        // 返回当前 Logger 包装类的全限定名。
        // SLF4J 底层如果需要提取代码行号，会跳过这个类，从而找到真正打日志的业务代码行。
        return CustomLogImpl.class.getName();
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] args, Throwable throwable) {
        if (!checkLevel(level.toInt())) {
            return;
        }

        // 仅将原始数据入队，避免在业务线程进行耗时的 MessageFormatter 操作
        LogEvent event = new LogEvent(
                LogLevel.valueOf(level.name()),
                msg,
                args,
                throwable,
                OperationContext.getOrNull(),
                name
        );

        if (marker != null) {
            event.setMarker(marker);
        }

        LogWriter.enqueue(event);
    }
}