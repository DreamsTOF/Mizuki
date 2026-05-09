# dreamtof-log 模块设计模式分析

## 模块概述

dreamtof-log 提供自定义日志基础设施，通过 SPI 替换 SLF4J 底层实现，支持异步日志处理和多种输出方式。

---

## 1. SPI 模式（Service Provider Interface）

### 实现位置
`cn.dreamtof.log.core.CustomSLF4JServiceProvider`

### 示例代码

```java
public class CustomSLF4JServiceProvider implements SLF4JServiceProvider {
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    @Override
    public void initialize() {
        loggerFactory = new CustomLoggerFactory();
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new BasicMDCAdapter();
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.99";
    }
}
```

### SPI 配置文件
`META-INF/services/org.slf4j.spi.SLF4JServiceProvider`

```
cn.dreamtof.log.core.CustomSLF4JServiceProvider
```

### 解决的问题
- 完全替换 SLF4J 的底层实现
- 实现自定义的日志处理逻辑，无需修改业务代码
- 通过 Java SPI 机制自动发现

---

## 2. 工厂方法模式（Factory Method Pattern）

### 实现位置
`cn.dreamtof.log.core.CustomLoggerFactory`

### 示例代码

```java
public class CustomLoggerFactory implements ILoggerFactory {
    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        return loggerMap.computeIfAbsent(name, CustomLogImpl::new);
    }

    public void clear() {
        loggerMap.clear();
    }
}
```

### 解决的问题
- 统一管理 Logger 实例的创建
- 缓存已创建的 Logger，避免重复实例化
- 使用 ConcurrentHashMap 保证线程安全

---

## 3. 适配器模式（Adapter Pattern）

### 实现位置
`cn.dreamtof.log.core.CustomLogImpl`

### 示例代码

```java
public class CustomLogImpl extends LegacyAbstractLogger {
    private final String name;

    public CustomLogImpl(String name) {
        this.name = name;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, 
                                               Object[] args, Throwable throwable) {
        LogEvent event = new LogEvent(
            name,
            level,
            msg,
            args,
            throwable,
            marker,
            Thread.currentThread().getName(),
            System.currentTimeMillis()
        );
        LogWriter.enqueue(event);
    }

    @Override
    public boolean isTraceEnabled() { return true; }
    @Override
    public boolean isDebugEnabled() { return true; }
    @Override
    public boolean isInfoEnabled() { return true; }
    @Override
    public boolean isWarnEnabled() { return true; }
    @Override
    public boolean isErrorEnabled() { return true; }
}
```

### 解决的问题
- 适配 SLF4J 的 Logger 接口
- 将日志调用转换为内部的 LogEvent 事件
- 继承 LegacyAbstractLogger 复用 SLF4J 的模板逻辑

---

## 4. 生产者-消费者模式（Producer-Consumer Pattern）

### 实现位置
`cn.dreamtof.log.core.LogWriter`

### 示例代码

```java
public class LogWriter implements Runnable {
    private final BlockingQueue<LogEvent> eventQueue;
    private final AtomicInteger running = new AtomicInteger(1);
    private static final LogWriter SINGLETON = new LogWriter();

    static {
        Thread.ofVirtual().name("Log-Bus-Dispatcher").start(SINGLETON);
        Runtime.getRuntime().addShutdownHook(new Thread(SINGLETON::stop));
    }

    private LogWriter() {
        LogProperties config = new LogProperties();
        this.eventQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());
    }

    // 生产者入口
    public static void enqueue(LogEvent event) {
        boolean success = SINGLETON.eventQueue.offer(event);
        if (!success) {
            // 背压丢弃策略：丢弃低级别日志
            if (event.level() != Level.ERROR && event.level() != Level.WARN) {
                return;
            }
            // ERROR/WARN 级别强制阻塞等待
            try {
                SINGLETON.eventQueue.put(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 消费者循环
    @Override
    public void run() {
        LogProperties config = new LogProperties();
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                LogEvent event = eventQueue.poll(config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
                if (event != null) {
                    dispatch(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running.set(0);
    }
}
```

### 解决的问题
- 异步解耦：业务线程不阻塞，日志写入在独立线程完成
- 背压保护：队列满时自动丢弃低级别日志
- 使用虚拟线程调度，提高并发性能

---

## 5. 观察者模式（Observer Pattern）

### 实现位置
`cn.dreamtof.log.spi.LogListener` + `cn.dreamtof.log.core.LogWriter`

### 示例代码

```java
// SPI 接口
public interface LogListener {
    void onLog(LogEvent event);
}

// 观察者注册与分发
public class LogWriter implements Runnable {
    private static final List<LogListener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void registerListener(LogListener listener) {
        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void unregisterListener(LogListener listener) {
        LISTENERS.remove(listener);
    }

    private void dispatch(LogEvent event) {
        for (LogListener listener : LISTENERS) {
            try {
                listener.onLog(event);
            } catch (Exception e) {
                // 隔离异常，防止单个 Listener 失败影响其他
                System.err.println("LogListener error: " + e.getMessage());
            }
        }
    }
}
```

### 典型实现示例

```java
// 控制台输出监听器
public class ConsoleLogListener implements LogListener {
    @Override
    public void onLog(LogEvent event) {
        System.out.println(formatEvent(event));
    }
}

// 文件输出监听器
public class FileLogListener implements LogListener {
    private final Path logPath;

    @Override
    public void onLog(LogEvent event) {
        Files.writeString(logPath, formatEvent(event) + "\n", 
                          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
```

### 解决的问题
- 支持多种日志输出方式（控制台、文件、Kafka 等）
- 新增输出方式只需实现 LogListener 接口并注册
- 异常隔离机制保证系统健壮性

---

## 6. 单例模式（Singleton Pattern）

### 实现位置
`cn.dreamtof.log.core.LogWriter`

### 示例代码

```java
public class LogWriter implements Runnable {
    private static final LogWriter SINGLETON = new LogWriter();

    static {
        // 在类加载时自动启动后台虚拟线程
        Thread.ofVirtual().name("Log-Bus-Dispatcher").start(SINGLETON);
        // 注册 JVM 关闭钩子实现优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(SINGLETON::stop));
    }

    private LogWriter() {
        this.eventQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());
    }

    public static LogWriter getInstance() {
        return SINGLETON;
    }
}
```

### 解决的问题
- 全局唯一的日志分发引擎
- 确保所有日志事件进入同一个队列
- 私有构造器 + 静态实例保证单例
- 在类加载时自动启动后台线程

---

## 7. 值对象模式（Value Object Pattern）

### 实现位置
`cn.dreamtof.log.core.LogEvent`

### 示例代码

```java
public record LogEvent(
    String loggerName,
    Level level,
    String message,
    Object[] args,
    Throwable throwable,
    Marker marker,
    String threadName,
    long timestamp
) {
    public String getFormattedMessage() {
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(message.replace("{}", "%s"), args);
    }
}
```

### 解决的问题
- 使用 Java Record 实现不可变值对象
- 封装日志事件的所有信息
- 线程安全，无需同步

---

## 设计模式总结

| 设计模式 | 核心应用场景 |
|---------|-------------|
| SPI 模式 | SLF4J 底层实现替换 |
| 工厂方法模式 | Logger 实例创建与管理 |
| 适配器模式 | SLF4J 接口适配 |
| 生产者-消费者模式 | 异步日志队列处理 |
| 观察者模式 | 多种日志输出方式 |
| 单例模式 | 全局日志分发引擎 |
| 值对象模式 | 日志事件封装 |

---

## 架构流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                        业务代码                                   │
│                    log.info("message")                          │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SLF4J API                                     │
│                    Logger 接口                                    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                 CustomLogImpl (适配器)                           │
│              handleNormalizedLoggingCall()                      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LogEvent (值对象)                             │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                 LogWriter (生产者-消费者)                        │
│              BlockingQueue<LogEvent>                            │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                 LogListener (观察者)                             │
│    ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│    │  Console    │  │    File     │  │   Kafka     │           │
│    │  Listener   │  │   Listener  │  │  Listener   │           │
│    └─────────────┘  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```
