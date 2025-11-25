# Java 全栈高级面试题汇总

本文档涵盖了 Java 基础、多线程、JVM、MySQL、Redis、消息队列、微服务 (Spring Cloud)、Netty 等核心领域的面试题与解析。

---

## 1. 高并发与架构设计

### 1.1 核心概念
*   **高可用 (HA)**: 服务冗余、故障转移。
*   **高并发治理**:
    *   **配置中心**: 动态管理配置 (Nacos, Apollo)。
    *   **服务注册与发现**: 动态感知服务实例 (Eureka, Nacos, Consul)。
    *   **负载均衡**: 流量分发 (Ribbon, Nginx)。
    *   **限流**: 保护系统不被流量打垮 (Sentinel, RateLimiter)。
    *   **熔断**: 下游服务不可用时快速失败，防止雪崩 (Hystrix, Sentinel)。
    *   **降级**: 核心服务优先，非核心服务返回兜底数据。
    *   **资源隔离**: 线程池隔离、信号量隔离 (Hystrix)。

### 1.2 项目案例：体育商户分组系统
*   **业务**: 不同商户 (c, s, y, b) 进入对应场馆，注单分流到对应数据库。
*   **数据流**: 业务库 -> FlinkSQL -> 汇总库/报表库。
*   **技术栈**: Spring Boot, Redis + Ehcache (二级缓存), 配置中心, 服务治理。
*   **缓存策略**: 避免缓存 Key 同时失效导致雪崩 (设置随机过期时间)。

---

## 2. Java 多线程与并发

### 2.1 线程创建方式
1.  **继承 Thread 类**: 单继承局限。
2.  **实现 Runnable 接口**: 推荐，支持多实现，资源共享。
3.  **实现 Callable 接口**: 可返回值，抛异常，配合 FutureTask。
4.  **线程池**: 资源复用，管理线程。

### 2.2 核心方法区别
*   **start() vs run()**: `start()` 启动新线程 (异步)；`run()` 只是普通方法调用 (同步)。
*   **wait() vs sleep()**:
    *   `wait()`: Object 类，释放锁，需在同步块中。
    *   `sleep()`: Thread 类，不释放锁。

### 2.3 线程池 (ThreadPoolExecutor)
*   **7大参数**: `corePoolSize`, `maximumPoolSize`, `keepAliveTime`, `unit`, `workQueue`, `threadFactory`, `handler`。
*   **拒绝策略**: `AbortPolicy` (抛异常), `CallerRunsPolicy` (调用者执行), `DiscardPolicy` (丢弃), `DiscardOldestPolicy` (丢弃最老)。
*   **配置建议**:
    *   CPU 密集型: CPU核数 + 1。
    *   IO 密集型: 2 * CPU核数。

### 2.4 锁机制
*   **synchronized**: JVM 层，自动释放，不可中断，非公平。锁升级：无锁 -> 偏向锁 -> 轻量级锁 -> 重量级锁。
*   **ReentrantLock**: API 层，手动释放，可中断，可公平。
*   **CAS (Compare And Swap)**: 乐观锁，原子操作。存在 ABA 问题 (加版本号解决)。
*   **死锁避免**: 避免嵌套锁、顺序加锁、定时锁 (tryLock)、死锁检测。

### 2.5 ThreadLocal
*   **原理**: 每个线程内部维护 `ThreadLocalMap`。
*   **内存泄漏**: Key 是弱引用，Value 是强引用。务必在 `finally` 中调用 `remove()`。

---

## 3. Java 基础

*   **String**: final 修饰，不可继承，不可变。
*   **StringBuilder vs StringBuffer**: Builder 非线程安全 (快)；Buffer 线程安全 (慢)。
*   **List vs Map**: List 有序可重复；Map 键值对，Key 唯一。
*   **ArrayList vs Vector**: ArrayList 非线程安全；Vector 线程安全 (synchronized)。
*   **HashMap vs Hashtable**: HashMap 非线程安全，允许 null；Hashtable 线程安全，不允许 null。
*   **HashMap 原理 (JDK 1.8)**: 数组 + 链表 + 红黑树。链表长度 > 8 转红黑树，< 6 转链表。扩容阈值 0.75。
*   **== vs equals**: `==` 比地址；`equals` 比内容 (需重写)。
*   **final**: 修饰类 (不可继承)、方法 (不可重写)、变量 (常量)。

---

## 4. MySQL 数据库

### 4.1 事务与隔离级别
*   **ACID**: 原子性、一致性、隔离性、持久性。
*   **隔离级别**:
    *   读未提交 (脏读)
    *   读已提交 (不可重复读)
    *   可重复读 (RR, 默认): 解决脏读、不可重复读。通过 MVCC + Next-Key Lock 解决幻读。
    *   串行化

### 4.2 索引与优化
*   **InnoDB vs MyISAM**: InnoDB 支持事务、行锁、外键，聚簇索引；MyISAM 不支持。
*   **索引失效**: `like '%...'`, `!=`, `or` (部分无索引), 函数计算, 类型转换。
*   **最左匹配原则**: 联合索引从最左边开始匹配。
*   **慢 SQL 优化**: 开启慢日志 -> Explain 分析 (type, rows, key) -> 优化索引 -> 优化 SQL 逻辑 (避免全表扫描, 减少 Join)。

### 4.3 分库分表
*   **垂直切分**: 按列拆分 (大字段独立)。
*   **水平切分**: 按行拆分 (分库分表, 时间切分)。
*   **中间件**: Mycat, ShardingSphere。

---

## 5. JVM 调优

*   **内存区域**: 堆 (Young, Old), 栈, 方法区。
*   **GC 算法**: 标记-清除, 复制 (Young), 标记-整理 (Old)。
*   **调优指标**: 吞吐量 (业务时间占比), 停顿时间 (STW)。
*   **G1 调优参数**: `-Xmx`, `-Xms`, `-XX:+UseG1GC`, `-XX:MaxGCPauseMillis=200`。
*   **内存泄漏**: 长生命周期对象持有短生命周期引用，未关闭资源。
*   **工具**: `jconsole`, `jvisualvm`, `jmap`, `jstack`, GCEasy。

---

## 6. Redis 缓存

*   **数据类型**: String, List, Set, Hash, ZSet。
*   **分布式锁**: Redisson (Watchdog 自动续期)。
*   **缓存问题**:
    *   **雪崩**: 大量 Key 同时失效 -> 随机过期时间。
    *   **击穿**: 热点 Key 失效 -> 互斥锁。
    *   **穿透**: 查询不存在的数据 -> 布隆过滤器, 缓存空对象。
*   **集群**: Hash 取模, 一致性 Hash, Hash Slot (Redis Cluster)。

---

## 7. 消息队列 (MQ)

### 7.1 场景与选型
*   **场景**: 削峰填谷, 异步处理, 解耦。
*   **选型**: Kafka (高吞吐), RocketMQ (可靠性, 事务), RabbitMQ (低延迟)。

### 7.2 核心问题
*   **消息丢失**: 生产端 (Ack), 服务端 (持久化/副本), 消费端 (手动 Ack)。
*   **重复消费**: 幂等性设计 (数据库唯一键, Redis 记录)。
*   **顺序消息**: 局部有序 (同一 Key 发往同一分区，单线程消费)。
*   **消息堆积**: 增加消费者，优化消费逻辑，临时扩容。
*   **事务消息**: 半消息 + 二次确认 (RocketMQ)。

---

## 8. 微服务与分布式 (Spring Cloud)

### 8.1 核心组件
*   **Eureka**: 服务注册与发现 (AP)。Client 心跳续约，Server 剔除。
*   **Ribbon**: 客户端负载均衡。轮询, 随机, 加权。
*   **Feign**: 声明式服务调用。
*   **Hystrix**: 熔断器 (开路/半开/闭路), 资源隔离 (线程池/信号量)。
*   **Zuul / Gateway**: 网关。路由, 过滤, 鉴权, 限流。
*   **Config**: 配置中心 (Git/SVN)。配合 Bus 自动刷新。

### 8.2 分布式事务
*   **2PC**: 两阶段提交 (准备, 提交/回滚)。单点故障, 阻塞。
*   **3PC**: 引入预提交，超时机制。
*   **TCC**: Try-Confirm-Cancel。灵活性高，代码侵入大。
*   **最终一致性**: 本地消息表, MQ 事务消息。

### 8.3 CAP 定理
*   C (一致性), A (可用性), P (分区容错性)。P 必选，CP (Zookeeper) vs AP (Eureka)。

---

## 9. Netty 网络编程

### 9.1 核心概念
*   **NIO**: 非阻塞 IO，Selector, Channel, Buffer。
*   **Reactor 模型**: 主从多线程模型 (BossGroup 接收连接, WorkerGroup 处理 IO)。
*   **零拷贝**: `DirectBuffer` (堆外内存), `CompositeByteBuf`, `FileRegion` (sendfile)。

### 9.2 核心组件
*   **Channel**: 连接。
*   **Pipeline**: 处理器链 (责任链模式)。
*   **Handler**: 业务逻辑 (Inbound/Outbound)。
*   **ByteBuf**: 读写索引分离，支持池化，自动扩容。

### 9.3 常见问题
*   **粘包/拆包**: TCP 字节流特性。解决：`LengthFieldBasedFrameDecoder` (长度字段), 定长, 分隔符。
*   **心跳机制**: `IdleStateHandler` 检测空闲，发送 Ping/Pong。

---

## 10. MyBatis

*   **核心**: SqlSessionFactory, SqlSession, Mapper。
*   **#{} vs ${}**: `#{}` 预编译 (防注入); `${}` 直接拼接。
*   **缓存**: 一级缓存 (SqlSession 级别, 默认开启); 二级缓存 (Mapper 级别, 需配置)。
*   **动态 SQL**: `if`, `choose`, `where`, `foreach`。
*   **插件原理**: 动态代理 (拦截 Executor, ParameterHandler, ResultSetHandler, StatementHandler)。
