# dreamtof-log 模块设计模式分析

## 模块概述

dreamtof-log 提供自定义日志基础设施，通过 SPI 替换 SLF4J 底层实现，支持异步日志处理和多种输出方式。

---

## 模块架构流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              dreamtof-log 架构总览                              │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          业务代码层                                      │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │                    log.info("用户登录: {}", userId)               │   │   │
│  │  │                    log.error("处理失败", exception)               │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          SLF4J API 层                                   │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │                    Logger 接口 (标准 API)                         │   │   │
│  │  │                    trace/debug/info/warn/error                    │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          SPI 适配层                                     │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐      │   │
│  │  │CustomSLF4JService│  │CustomLoggerFactory│  │  CustomLogImpl   │      │   │
│  │  │   Provider       │  │   (工厂模式)      │  │   (适配器模式)    │      │   │
│  │  │   (SPI模式)      │  │                  │  │                  │      │   │
│  │  │                  │  │ 创建 Logger 实例  │  │ SLF4J → LogEvent │      │   │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘      │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          异步处理层                                     │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │                    LogWriter (生产者-消费者 + 单例)               │   │   │
│  │  │                                                                   │   │   │
│  │  │   ┌─────────────────┐         ┌─────────────────┐                │   │   │
│  │  │   │ BlockingQueue   │         │  虚拟线程消费者  │                │   │   │
│  │  │   │ (生产者入队)     │ ──────► │  (消费分发)      │                │   │   │
│  │  │   └─────────────────┘         └─────────────────┘                │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          输出监听层                                     │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐      │   │
│  │  │ConsoleLogListener│  │ FileLogListener  │  │ KafkaLogListener │      │   │
│  │  │   (观察者)       │  │   (观察者)        │  │   (观察者)        │      │   │
│  │  │                  │  │                  │  │                  │      │   │
│  │  │ 输出到控制台      │  │ 写入文件         │  │ 发送到 Kafka      │      │   │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘      │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 1. SPI 模式（Service Provider Interface）

### 什么是 SPI 模式？

SPI（Service Provider Interface）是一种**服务发现机制**，它允许框架定义服务接口，由第三方提供具体实现，并通过配置文件在运行时动态发现和加载实现类。它实现了接口定义与实现的解耦，支持可插拔架构。

### 核心特征

1. **接口定义**：框架定义标准服务接口
2. **配置驱动**：通过配置文件声明实现类
3. **动态加载**：运行时通过 ServiceLoader 发现实现
4. **可插拔**：替换实现只需修改配置，无需改代码

### 为什么这是 SPI 模式而不是策略模式？

| 对比维度 | SPI 模式 | 策略模式 |
|---------|---------|---------|
| **发现机制** | 配置文件动态发现 | 代码中显式选择 |
| **扩展方式** | 第三方可提供实现 | 需要修改客户端代码 |
| **加载时机** | 运行时动态加载 | 编译时确定策略类 |
| **典型场景** | JDBC驱动、日志框架、序列化框架 | 算法选择、支付方式选择 |

**判断依据**：`CustomSLF4JServiceProvider` 通过 `META-INF/services/` 配置文件声明，SLF4J 框架在运行时通过 `ServiceLoader` 动态发现并加载。这是**框架级别的服务发现机制**，允许用户在不修改 SLF4J 代码的情况下替换底层实现，这符合 SPI 模式的定义。

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
    public String getRequestedApiVersion() {
        return "2.0.99";
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SPI 模式工作流程                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    应用启动                                                                      │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      SLF4J 初始化                                    │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              查找 SLF4JServiceProvider                     │    │     │
│    │   │                                                           │    │     │
│    │   │   ServiceLoader.load(SLF4JServiceProvider.class)          │    │     │
│    │   │       │                                                   │    │     │
│    │   │       ▼                                                   │    │     │
│    │   │   读取 META-INF/services/org.slf4j.spi.SLF4JServiceProvider│   │     │
│    │   │       │                                                   │    │     │
│    │   │       ▼                                                   │    │     │
│    │   │   发现: cn.dreamtof.log.core.CustomSLF4JServiceProvider   │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              CustomSLF4JServiceProvider.initialize()       │    │     │
│    │   │                                                           │    │     │
│    │   │   @Override                                               │    │     │
│    │   │   public void initialize() {                              │    │     │
│    │   │       loggerFactory = new CustomLoggerFactory();          │    │     │
│    │   │       markerFactory = new BasicMarkerFactory();           │    │     │
│    │   │       mdcAdapter = new BasicMDCAdapter();                 │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    SPI 配置文件：                                                               │
│    META-INF/services/org.slf4j.spi.SLF4JServiceProvider                        │
│    内容: cn.dreamtof.log.core.CustomSLF4JServiceProvider                        │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 完全替换 SLF4J 的底层实现
- 实现自定义的日志处理逻辑，无需修改业务代码

---

## 2. 工厂方法模式（Factory Method Pattern）

### 什么是工厂方法模式？

工厂方法模式是一种创建型设计模式，**定义一个创建对象的接口，但由子类决定要实例化的类是哪一个**。工厂方法让类把实例化推迟到子类，客户端无需知道具体创建的类。

### 核心特征

1. **抽象创建**：定义抽象的工厂接口
2. **延迟实例化**：由子类决定具体实例化哪个类
3. **解耦客户端**：客户端只依赖抽象接口
4. **扩展方便**：新增产品只需新增工厂实现

### 为什么这是工厂方法模式而不是简单工厂？

| 对比维度 | 工厂方法模式 | 简单工厂 |
|---------|------------|---------|
| **工厂结构** | 抽象工厂 + 具体工厂 | 单一工厂类 |
| **扩展方式** | 新增工厂子类 | 修改工厂方法 |
| **开闭原则** | 符合开闭原则 | 需修改已有代码 |
| **典型场景** | 框架级扩展点 | 简单对象创建 |

**判断依据**：`CustomLoggerFactory` 实现了 SLF4J 的 `ILoggerFactory` 接口，这是一个**抽象的工厂接口**。SLF4J 框架定义了工厂接口，由具体实现（CustomLoggerFactory）决定创建哪种 Logger 实例。这符合工厂方法模式"由子类决定实例化"的定义。

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

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              工厂方法模式流程                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码获取 Logger                                                           │
│    LoggerFactory.getLogger(UserService.class)                                  │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      CustomLoggerFactory                             │     │
│    │                                                                     │     │
│    │   implements ILoggerFactory (SLF4J 接口)                            │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              getLogger(String name)                        │    │     │
│    │   │                                                           │    │     │
│    │   │   @Override                                               │    │     │
│    │   │   public Logger getLogger(String name) {                  │    │     │
│    │   │       return loggerMap.computeIfAbsent(name, key -> {     │    │     │
│    │   │           return new CustomLogImpl(key);  // 工厂方法     │    │     │
│    │   │       });                                                 │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              Logger 缓存管理                               │    │     │
│    │   │                                                           │    │     │
│    │   │   ConcurrentMap<String, Logger> loggerMap                 │    │     │
│    │   │                                                           │    │     │
│    │   │   "com.example.UserService"  → CustomLogImpl@1            │    │     │
│    │   │   "com.example.OrderService" → CustomLogImpl@2            │    │     │
│    │   │   "com.example.PaymentService" → CustomLogImpl@3          │    │     │
│    │   │                                                           │    │     │
│    │   │   特性:                                                    │    │     │
│    │   │   - 首次获取时创建 Logger 实例                             │    │     │
│    │   │   - 后续获取直接返回缓存实例                               │    │     │
│    │   │   - ConcurrentHashMap 保证线程安全                         │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 统一管理 Logger 实例的创建
- 缓存已创建的 Logger，避免重复实例化

---

## 3. 适配器模式（Adapter Pattern）

### 什么是适配器模式？

适配器模式是一种结构型设计模式，**将一个类的接口转换成客户端期望的另一个接口**。它使原本因接口不兼容而不能一起工作的类可以协同工作，解决了接口不匹配的问题。

### 核心特征

1. **接口转换**：将现有接口转换为目标接口
2. **复用现有类**：无需修改原有代码即可复用
3. **解耦客户端**：客户端通过统一接口与不同实现交互
4. **透明适配**：客户端无需知道适配过程

### 为什么这是适配器模式而不是代理模式？

| 对比维度 | 适配器模式 | 代理模式 |
|---------|-----------|---------|
| **主要目的** | 接口转换 | 控制访问 |
| **接口关系** | 改变接口以匹配目标 | 保持接口不变 |
| **使用场景** | 接口不兼容时 | 需要控制访问时 |
| **典型例子** | 日志框架适配、电源适配器 | 远程代理、保护代理 |

**判断依据**：`CustomLogImpl` 继承 `LegacyAbstractLogger`（SLF4J 抽象类），将 SLF4J 的日志调用接口**转换**为内部的 `LogEvent` 事件并提交到队列。它不是为了控制访问，而是为了**让自定义日志系统适配 SLF4J 的标准接口**，这符合适配器模式的定义。

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
            name, level, msg, args, throwable, marker,
            Thread.currentThread().getName(),
            System.currentTimeMillis()
        );
        LogWriter.enqueue(event);
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              适配器模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码调用 SLF4J API                                                        │
│    log.info("用户登录: {}", userId)                                             │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      SLF4J Logger 接口                               │     │
│    │                                                                     │     │
│    │   void info(String msg, Object... args)                             │     │
│    │   void error(String msg, Throwable t)                               │     │
│    │                                                                     │     │
│    └───────────────────────────────┬─────────────────────────────────────┘     │
│                                    │                                          │
│                                    ▼                                          │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  CustomLogImpl (适配器)                              │     │
│    │                                                                     │     │
│    │   extends LegacyAbstractLogger (继承 SLF4J 抽象类)                   │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              适配转换过程                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   SLF4J 调用                     内部 LogEvent             │    │     │
│    │   │   ┌──────────────────┐         ┌──────────────────┐      │    │     │
│    │   │   │ log.info(        │         │ LogEvent {       │      │    │     │
│    │   │   │   "用户登录: {}",│  适配   │   loggerName:    │      │    │     │
│    │   │   │   userId         │ ────►  │     "UserService" │      │    │     │
│    │   │   │ )                │         │   level: INFO    │      │    │     │
│    │   │   └──────────────────┘         │   message:       │      │    │     │
│    │   │                                │     "用户登录: 1001"│     │    │     │
│    │   │                                │   timestamp:     │      │    │     │
│    │   │                                │     1234567890   │      │    │     │
│    │   │                                │ }                │      │    │     │
│    │   │                                └──────────────────┘      │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   handleNormalizedLoggingCall()                                     │     │
│    │       LogEvent event = new LogEvent(...);                           │     │
│    │       LogWriter.enqueue(event);  // 提交到异步队列                   │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 适配 SLF4J 的 Logger 接口
- 将日志调用转换为内部的 LogEvent 事件

---

## 4. 生产者-消费者模式（Producer-Consumer Pattern）

### 什么是生产者-消费者模式？

生产者-消费者模式是一种**并发设计模式**，它将数据的生产和消费解耦，通过队列作为缓冲区，生产者将数据放入队列，消费者从队列取出数据处理。它实现了生产者和消费者之间的解耦和异步通信。

### 核心特征

1. **解耦生产消费**：生产者和消费者独立运行
2. **缓冲队列**：队列作为中间缓冲区平衡速度差异
3. **异步处理**：生产者无需等待消费者处理完成
4. **背压控制**：可通过队列容量控制处理压力

### 为什么这是生产者-消费者模式而不是观察者模式？

| 对比维度 | 生产者-消费者模式 | 观察者模式 |
|---------|------------------|-----------|
| **通信方式** | 通过队列中转 | 直接通知 |
| **时序关系** | 异步、解耦 | 同步或异步 |
| **缓冲能力** | 有缓冲队列 | 无缓冲 |
| **典型场景** | 消息队列、日志处理 | 事件监听、GUI事件 |

**判断依据**：`LogWriter` 使用 `BlockingQueue` 作为**缓冲队列**，业务线程（生产者）将日志事件放入队列后立即返回，后台虚拟线程（消费者）从队列取出事件分发给监听器。生产者和消费者通过队列**解耦**，支持异步处理和背压控制，这符合生产者-消费者模式的定义。

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
        this.eventQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());
    }

    public static void enqueue(LogEvent event) {
        boolean success = SINGLETON.eventQueue.offer(event);
        if (!success) {
            // 背压丢弃策略
            if (event.level() != Level.ERROR && event.level() != Level.WARN) {
                return;
            }
            SINGLETON.eventQueue.put(event);
        }
    }

    @Override
    public void run() {
        while (running.get() || !eventQueue.isEmpty()) {
            LogEvent event = eventQueue.poll(config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
            if (event != null) {
                dispatch(event);
            }
        }
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              生产者-消费者模式流程                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                          生产者 (多线程)                              │     │
│    │                                                                     │     │
│    │   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐              │     │
│    │   │ HTTP 请求1  │   │ HTTP 请求2  │   │ 定时任务    │              │     │
│    │   │ log.info()  │   │ log.error() │   │ log.debug() │              │     │
│    │   └──────┬──────┘   └──────┬──────┘   └──────┬──────┘              │     │
│    │          │                 │                 │                      │     │
│    │          └─────────────────┼─────────────────┘                      │     │
│    │                            │                                        │     │
│    │                            ▼                                        │     │
│    │   LogWriter.enqueue(event)                                         │     │
│    │       eventQueue.offer(event)  // 非阻塞入队                        │     │
│    │                                                                     │     │
│    │   背压策略:                                                          │     │
│    │   - 队列未满: 直接入队                                              │     │
│    │   - 队列已满 + INFO/DEBUG: 丢弃                                     │     │
│    │   - 队列已满 + ERROR/WARN: 阻塞等待                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                      │                                          │
│                                      ▼                                          │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      BlockingQueue (缓冲区)                          │     │
│    │                                                                     │     │
│    │   LinkedBlockingQueue<LogEvent> (容量: 10000)                       │     │
│    │                                                                     │     │
│    │   ┌───────┬───────┬───────┬───────┬───────┬─────────────────┐      │     │
│    │   │ Event │ Event │ Event │ Event │ Event │ ... (空位)      │      │     │
│    │   │  1    │  2    │  3    │  4    │  5    │                 │      │     │
│    │   └───────┴───────┴───────┴───────┴───────┴─────────────────┘      │     │
│    │                                                                     │     │
│    │   特性: 线程安全、有界队列、非阻塞 offer / 阻塞 put                   │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                      │                                          │
│                                      ▼                                          │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      消费者 (单虚拟线程)                              │     │
│    │                                                                     │     │
│    │   Thread.ofVirtual().name("Log-Bus-Dispatcher").start(SINGLETON)   │     │
│    │                                                                     │     │
│    │   run() 消费循环:                                                    │     │
│    │   while (running.get() || !eventQueue.isEmpty()) {                  │     │
│    │       LogEvent event = eventQueue.poll(100, MILLISECONDS);          │     │
│    │       if (event != null) {                                          │     │
│    │           dispatch(event);  // 分发给监听器                          │     │
│    │       }                                                             │     │
│    │   }                                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    性能优势：                                                                    │
│    1. 异步解耦：业务线程不阻塞                                                   │
│    2. 背压保护：队列满时自动丢弃低级别日志                                        │
│    3. 虚拟线程：轻量级调度，高并发                                               │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 异步解耦：业务线程不阻塞
- 背压保护：队列满时自动丢弃低级别日志
- 使用虚拟线程调度，提高并发性能

---

## 5. 观察者模式（Observer Pattern）

### 什么是观察者模式？

观察者模式是一种行为型设计模式，**定义对象间的一对多依赖关系，当一个对象（被观察者）状态改变时，所有依赖于它的对象（观察者）都会收到通知并自动更新**。它实现了发布-订阅机制，使对象之间松耦合。

### 核心特征

1. **一对多关系**：一个被观察者可以有多个观察者
2. **自动通知**：被观察者状态变化时自动通知所有观察者
3. **松耦合**：被观察者不需要知道具体观察者的实现
4. **动态订阅**：观察者可以动态注册和注销

### 为什么这是观察者模式而不是责任链模式？

| 对比维度 | 观察者模式 | 责任链模式 |
|---------|-----------|-----------|
| **通知方式** | 广播给所有观察者 | 沿链条传递给一个处理者 |
| **处理者数量** | 多个观察者同时处理 | 通常一个处理者处理 |
| **链条关系** | 观察者之间无关联 | 处理者形成链条 |
| **典型场景** | 事件监听、消息推送 | 审批流程、异常处理 |

**判断依据**：`LogListener` 的实现类（ConsoleLogListener、FileLogListener、KafkaLogListener）都**独立接收**同一个日志事件，它们之间没有链条关系，不存在传递和中断。一个日志事件会被**所有注册的监听器同时处理**，这符合观察者模式的定义。

### 实现位置
`cn.dreamtof.log.spi.LogListener` + `cn.dreamtof.log.core.LogWriter`

### 示例代码

```java
public interface LogListener {
    void onLog(LogEvent event);
}

public class LogWriter implements Runnable {
    private static final List<LogListener> LISTENERS = new CopyOnWriteArrayList<>();

    public static void registerListener(LogListener listener) {
        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    private void dispatch(LogEvent event) {
        for (LogListener listener : LISTENERS) {
            try {
                listener.onLog(event);
            } catch (Exception e) {
                // 隔离异常，防止单个 Listener 失败影响其他
            }
        }
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              观察者模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    LogWriter 消费日志事件                                                        │
│    dispatch(event)                                                             │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      LogListener 注册表                              │     │
│    │                                                                     │     │
│    │   CopyOnWriteArrayList<LogListener> LISTENERS                       │     │
│    │                                                                     │     │
│    │   注册监听器:                                                        │     │
│    │   LogWriter.registerListener(new ConsoleLogListener());             │     │
│    │   LogWriter.registerListener(new FileLogListener());                │     │
│    │   LogWriter.registerListener(new KafkaLogListener());               │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                      │                                          │
│                                      ▼                                          │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      dispatch(event) 分发                            │     │
│    │                                                                     │     │
│    │   for (LogListener listener : LISTENERS) {                          │     │
│    │       try {                                                         │     │
│    │           listener.onLog(event);                                    │     │
│    │       } catch (Exception e) {                                       │     │
│    │           // 异常隔离，防止单个监听器失败影响其他                    │     │
│    │       }                                                             │     │
│    │   }                                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                      │                                          │
│         ┌─────────────────┬─────────────────┬─────────────────┐               │
│         ▼                 ▼                 ▼                 ▼               │
│    ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐     │
│    │Console      │   │File         │   │Kafka        │   │自定义       │     │
│    │LogListener  │   │LogListener  │   │LogListener  │   │LogListener  │     │
│    │             │   │             │   │             │   │             │     │
│    │输出到控制台  │   │写入日志文件 │   │发送到Kafka  │   │自定义处理   │     │
│    │             │   │             │   │             │   │             │     │
│    │System.out   │   │Files.write  │   │kafkaTemplate│   │...          │     │
│    │.println()   │   │             │   │.send()      │   │             │     │
│    └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘     │
│                                                                                 │
│    典型监听器实现：                                                              │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   public class ConsoleLogListener implements LogListener {          │     │
│    │       @Override                                                     │     │
│    │       public void onLog(LogEvent event) {                           │     │
│    │           System.out.println(formatEvent(event));                   │     │
│    │       }                                                             │     │
│    │   }                                                                 │     │
│    │                                                                     │     │
│    │   public class FileLogListener implements LogListener {             │     │
│    │       @Override                                                     │     │
│    │       public void onLog(LogEvent event) {                           │     │
│    │           Files.writeString(logPath, formatEvent(event) + "\n");    │     │
│    │       }                                                             │     │
│    │   }                                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 支持多种日志输出方式（控制台、文件、Kafka 等）
- 新增输出方式只需实现 LogListener 接口并注册
- 异常隔离机制保证系统健壮性

---

## 6. 单例模式（Singleton Pattern）

### 什么是单例模式？

单例模式是一种创建型设计模式，**确保一个类只有一个实例，并提供一个全局访问点**。它限制了类的实例化，确保在整个应用程序中只存在一个实例，常用于管理共享资源或全局状态。

### 核心特征

1. **唯一实例**：整个应用中只存在一个实例
2. **全局访问**：提供全局访问点获取实例
3. **私有构造**：防止外部通过 new 创建实例
4. **线程安全**：多线程环境下保证唯一实例

### 为什么这是单例模式而不是静态工具类？

| 对比维度 | 单例模式 | 静态工具类 |
|---------|---------|-----------|
| **实例化** | 有实例对象 | 无实例，全是静态方法 |
| **状态管理** | 可以维护实例状态 | 通常无状态 |
| **生命周期** | 可以延迟加载、可销毁 | 类加载时初始化 |
| **接口实现** | 可以实现接口 | 不能实现接口 |

**判断依据**：`LogWriter` 使用 `private static final` 持有唯一实例，私有构造器防止外部实例化，提供 `getInstance()` 全局访问点。它维护了 `BlockingQueue` 和 `running` 状态，并在静态初始化块中启动后台线程。这种**全局唯一实例 + 状态管理**的设计符合单例模式的定义。

### 实现位置
`cn.dreamtof.log.core.LogWriter`

### 示例代码

```java
public class LogWriter implements Runnable {
    private static final LogWriter SINGLETON = new LogWriter();

    static {
        Thread.ofVirtual().name("Log-Bus-Dispatcher").start(SINGLETON);
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

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              单例模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    类加载时初始化                                                                │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      LogWriter 单例                                  │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              单例实现方式                                   │    │     │
│    │   │                                                           │    │     │
│    │   │   private static final LogWriter SINGLETON = new LogWriter();│   │
│    │   │                                                           │    │     │
│    │   │   private LogWriter() {  // 私有构造器                     │    │     │
│    │   │       this.eventQueue = new LinkedBlockingQueue<>(...);   │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              静态初始化块                                   │    │     │
│    │   │                                                           │    │     │
│    │   │   static {                                                 │    │     │
│    │   │       // 1. 启动后台虚拟线程                               │    │     │
│    │   │       Thread.ofVirtual()                                  │    │     │
│    │   │           .name("Log-Bus-Dispatcher")                     │    │     │
│    │   │           .start(SINGLETON);                              │    │     │
│    │   │                                                           │    │     │
│    │   │       // 2. 注册 JVM 关闭钩子                              │    │     │
│    │   │       Runtime.getRuntime()                                │    │     │
│    │   │           .addShutdownHook(new Thread(SINGLETON::stop));  │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              单例优势                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   1. 全局唯一的日志分发引擎                                 │    │     │
│    │   │   2. 所有日志事件进入同一个队列                             │    │     │
│    │   │   3. 私有构造器 + 静态实例保证单例                          │    │     │
│    │   │   4. 类加载时自动启动后台线程                               │    │     │
│    │   │   5. JVM 关闭时优雅停机                                     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    JVM 关闭钩子流程：                                                            │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   JVM 收到关闭信号 (SIGTERM / SIGINT)                               │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   执行所有注册的 ShutdownHook                                       │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   LogWriter.stop()                                                  │     │
│    │       running.set(0);  // 设置停止标志                              │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   消费者线程继续处理队列中剩余的日志                                 │     │
│    │   while (running.get() || !eventQueue.isEmpty()) { ... }           │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   队列为空后线程退出                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 全局唯一的日志分发引擎
- 确保所有日志事件进入同一个队列
- 私有构造器 + 静态实例保证单例

---

## 7. 值对象模式（Value Object Pattern）

### 什么是值对象模式？

值对象模式是一种**通过对象的值而非身份来定义相等性**的设计模式。值对象是不可变的，两个值对象相等当且仅当它们的值相等。它强调对象的属性值而非对象引用，常用于表示度量、数量或描述性信息。

### 核心特征

1. **不可变性**：创建后状态不可改变
2. **值相等**：通过属性值判断相等，而非引用
3. **无副作用**：方法不修改对象状态
4. **自我验证**：创建时验证值的有效性

### 为什么这是值对象模式而不是数据传输对象（DTO）？

| 对比维度 | 值对象模式 | 数据传输对象（DTO） |
|---------|-----------|-------------------|
| **可变性** | 不可变 | 通常可变 |
| **相等判断** | 基于值 | 基于引用 |
| **行为逻辑** | 可包含业务行为 | 通常是纯数据容器 |
| **生命周期** | 可被共享复用 | 每次传输创建新实例 |

**判断依据**：`LogEvent` 使用 Java `record` 实现，天然具有**不可变性**。它通过属性值（loggerName、level、message等）定义相等性，包含 `getFormattedMessage()` 等行为方法。日志事件一旦创建就不会被修改，可以安全地在多线程间传递，这符合值对象模式的定义。

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

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              值对象模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    日志事件创建                                                                  │
│    new LogEvent(loggerName, level, message, args, ...)                         │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      LogEvent (Record 类型)                          │     │
│    │                                                                     │     │
│    │   Java Record 特性:                                                  │     │
│    │   - 不可变对象 (Immutable)                                           │     │
│    │   - 自动生成 equals/hashCode/toString                               │     │
│    │   - 自动生成构造器和访问器                                           │     │
│    │   - 线程安全，无需同步                                               │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              LogEvent 结构                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ loggerName: "com.example.UserService"           │    │    │     │
│    │   │   │ level: INFO                                     │    │    │     │
│    │   │   │ message: "用户登录: {}"                          │    │    │     │
│    │   │   │ args: [1001]                                    │    │    │     │
│    │   │   │ throwable: null                                 │    │    │     │
│    │   │   │ marker: null                                    │    │    │     │
│    │   │   │ threadName: "http-nio-8080-exec-1"             │    │    │     │
│    │   │   │ timestamp: 1234567890123                        │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              值对象方法                                     │    │     │
│    │   │                                                           │    │     │
│    │   │   // 格式化消息                                            │    │     │
│    │   │   public String getFormattedMessage() {                   │    │     │
│    │   │       if (args == null || args.length == 0) {             │    │     │
│    │   │           return message;                                 │    │     │
│    │   │       }                                                   │    │     │
│    │   │       return String.format(                               │    │     │
│    │   │           message.replace("{}", "%s"), args               │    │     │
│    │   │       );                                                  │    │     │
│    │   │   }                                                       │    │     │
│    │   │   // 结果: "用户登录: 1001"                                │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    值对象优势：                                                                  │
│    1. 不可变：线程安全，无需同步                                                │
│    2. 简洁：Java Record 自动生成样板代码                                        │
│    3. 安全：无法修改，防止意外变更                                              │
│    4. 高效：适合传递和缓存                                                      │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 使用 Java Record 实现不可变值对象
- 封装日志事件的所有信息
- 线程安全，无需同步

---

## 设计模式总结

| 设计模式 | 核心应用场景 | 关键类 | 判断依据 |
|---------|-------------|--------|---------|
| SPI 模式 | SLF4J 底层实现替换 | CustomSLF4JServiceProvider | 配置文件动态发现，框架级扩展点 |
| 工厂方法模式 | Logger 实例创建与管理 | CustomLoggerFactory | 抽象工厂接口，子类决定实例化 |
| 适配器模式 | SLF4J 接口适配 | CustomLogImpl | 接口转换适配，非控制访问 |
| 生产者-消费者模式 | 异步日志队列处理 | LogWriter | 队列缓冲解耦，异步处理 |
| 观察者模式 | 多种日志输出方式 | LogListener | 广播通知所有监听器，非链条传递 |
| 单例模式 | 全局日志分发引擎 | LogWriter | 全局唯一实例，维护队列状态 |
| 值对象模式 | 日志事件封装 | LogEvent | 不可变、值相等、线程安全 |
