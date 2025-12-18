# 华盛集团 - 高级 Java 开发/架构师 面试准备

根据岗位描述 (JD) 定制的针对性面试题库。

## 🎯 岗位核心要求分析

*   **基础扎实**: IO, 反射, 多线程, 集合, JVM (原理级理解)
*   **框架原理**: Spring Cloud, MyBatis (不仅是使用，由于强调"原理和机制")
*   **分布式能力**: 缓存 (Redis), 消息队列 (MQ), 高并发/高可用架构
*   **数据存储**: MySQL, Elasticsearch, ClickHouse (SQL 性能优化)
*   **软技能**: 业务理解, 优雅代码, 技术追求

---

## 1. Java 核心基础 (扎实功底)

### 1.1 反射 (Reflection)
> **JD 强调点**: 明确提到了"反射"，通常用于考察对框架底层实现的理解。

*   **Q: 什么是反射？它在 Spring/MyBatis 框架中是如何应用的？**
    *   **定义**: 在运行时动态获取类信息（属性、方法、构造器）并进行调用的机制。
    *   **框架应用**:
        *   **Spring IOC**: 解析 XML/注解，通过反射实例化 Bean (`Class.forName`, `newInstance`)。
        *   **Spring AOP**: 动态代理 (`Proxy.newProxyInstance`)，拦截方法调用。
        *   **MyBatis**: 结果集映射 (ResultSet -> Java Bean)，通过反射设置属性值。
    *   **缺点**: 性能损耗（动态解析类型）、破坏封装性（可访问 private）。

*   **Q: `Class.forName()` 和 `ClassLoader.loadClass()` 的区别？**
    *   `Class.forName()`: 加载类并**执行静态初始化块 (static block)**（如 JDBC 驱动注册）。
    *   `ClassLoader.loadClass()`: 只加载类文件，**不执行静态初始化**（延迟加载）。

### 1.2 多线程与并发 (Multi-threading)
> **JD 强调点**: 高并发系统的基础。

*   **Q: 线程池 `ThreadPoolExecutor` 的核心参数与工作流程？**
    *   **参数**: `corePoolSize` (核心), `maximumPoolSize` (最大), `workQueue` (队列), `keepAliveTime`, `handler` (拒绝策略)。
    *   **流程**: 核心线程 -> 阻塞队列 -> 最大线程 -> 拒绝策略。
    *   **考察**: 针对 IO 密集型 vs CPU 密集型如何设置参数？（IO 密集型通常设为 2N，CPU 密集型 N+1）。

*   **Q: `Synchronized` 锁升级过程是怎样的？**
    *   无锁 -> 偏向锁 (单线程访问) -> 轻量级锁 (CAS 自旋，多线程交替执行) -> 重量级锁 (OS Mutex，线程阻塞)。

### 1.3 集合 (Collections)
*   **Q: `HashMap` 的底层原理？为什么 JDK 8 引入红黑树？**
    *   **结构**: 数组 + 链表 + 红黑树。
    *   **JDK 8 变化**: 当链表长度 > 8 且数组长度 > 64 时，转为红黑树，将查找复杂度从 O(n) 降低到 O(log n)，解决哈希碰撞攻击或严重哈希冲突导致的性能退化。
    *   **扩容**: 负载因子 (0.75)，扩容为原来的 2 倍（保证 hash & (length-1) 的位运算优化）。

### 1.4 IO 模型
*   **Q: BIO, NIO, AIO 的区别？Redis/Netty 使用的是哪种？**
    *   **BIO**: 同步阻塞，一个连接一个线程。
    *   **NIO**: 同步非阻塞，多路复用 (Selector, Channel, Buffer)，一个线程处理多个连接。
    *   **AIO**: 异步非阻塞，OS 完成 IO 后回调通知。
    *   **Netty/Redis**: 基于 NIO (多路复用)。Redis 是单线程 Reactor 模型（6.0 后有多线程 IO）；Netty 是主从 Reactor 多线程模型。

---

## 2. JVM 原理

> **JD 强调点**: 对 JVM 原理有一定的了解。

*   **Q: 生产环境 CPU 飙高如何排查？**
    1.  `top` 找到高 CPU 进程 PID。
    2.  `top -Hp <PID>` 找到高 CPU 线程 ID (TID)。
    3.  `printf "%x\n" <TID>` 将 TID 转为 16 进制。
    4.  `jstack <PID> | grep <16进制TID> -A 20` 查看线程栈。
    5.  **原因**: 通常是死循环、无效的频繁 GC、或者大量复杂计算。

*   **Q: CMS 和 G1 垃圾收集器的区别？**
    *   **CMS**: 标记-清除算法，关注低停顿，容易产生碎片，老年代收集器。
    *   **G1**: 标记-整理 + 复制算法，将堆划分为多个 Region，可预测停顿时间 (Pause Prediction Model)，适合大内存机器，Java 9+ 默认。

---

## 3. 框架原理 (Spring & MyBatis)

> **JD 强调点**: 能了解到它的原理以及机制。

### 3.1 Spring
*   **Q: Spring 事务的实现原理及失效场景？**
    *   **原理**: AOP 动态代理。在目标方法前后进行 `conn.setAutoCommit(false)` 和 `commit/rollback`。
    *   **失效**: 方法自调用 (`this.method`)、非 `public` 方法、异常被 try-catch 吞掉、未配置 `rollbackFor` (默认只回滚 RuntimeException)。
*   **Q: Spring Bean 的生命周期？**
    *   实例化 -> 属性赋值 -> 初始化 (`Aware` 回调 -> `BeanPostProcessor.before` -> `PostConstruct` -> `BeanPostProcessor.after`) -> 使用 -> 销毁。
*   **Q: 循环依赖如何解决？**
    *   三级缓存机制 (`singletonObjects`, `earlySingletonObjects`, `singletonFactories`)，提前暴露半成品的代理对象引用。

### 3.2 MyBatis
*   **Q: MyBatis 也就是半自动 ORM，它的核心执行流程？**
    *   读取配置 -> 创建 `SqlSessionFactory` -> 开启 `SqlSession` -> 获取 Mapper 接口代理 -> 执行 SQL (通过 `Executor` -> `StatementHandler` -> `ParameterHandler` -> `ResultSetHandler`)。
*   **Q: `#` 和 `$` 的区别？**
    *   `#`: 预编译 (`PreparedStatement`)，防止 SQL 注入。
    *   `$`: 字符串替换，有注入风险，用于动态表名/列名。

---

## 4. 分布式系统设计

> **JD 强调点**: 缓存、消息机制、解决方案。

### 4.1 缓存 (Redis)
*   **Q: 缓存穿透、击穿、雪崩的区别及解决方案？**
    *   **穿透**: 查询不存在的数据 (Redis 无, DB 无)。解法：布隆过滤器、缓存空对象。
    *   **击穿**: 热点 Key 过期，大量请求直打 DB。解法：互斥锁 (Mutex Key)、逻辑过期 (永不过期，后台更新)。
    *   **雪崩**: 大量 Key 同时过期。解法：过期时间加随机值、Redis 集群高可用。
*   **Q: 怎么保证 DB 和 Redis 的数据一致性？**
    *   **延迟双删**: 更新 DB -> 删除缓存 -> 延时 -> 再次删除缓存。
    *   **Canal**: 监听 MySQL Binlog 异步更新缓存 (最终一致性)。

### 4.2 消息队列 (MQ)
*   **Q: MQ 如何保证消息不丢失 (以 RocketMQ/Kafka 为例)？**
    *   **发送端**: 确认机制 (Ack/Sync Send)。
    *   **Broker**: 持久化 (fsync), 多副本同步 (Replica)。
    *   **消费端**: 手动 Ack (处理完业务再确认)。
*   **Q: 如果遇到消息积压怎么处理？**
    *   临时扩容消费端，增加消费者数量。
    *   如果消费端有瓶颈，写临时程序将消息快速转存到另一个 Topic，通过更多消费者慢慢处理。

---

## 5. 数据存储 (MySQL/ES/ClickHouse)

> **JD 强调点**: SQL 性能优化经验，ES/CK。

### 5.1 MySQL
*   **Q: SQL 慢查询优化思路？**
    1.  看 `Explain` 执行计划 (type, key, rows, extra)。
    2.  **索引失效**: 避免 `LIKE '%xxx'`, 避免对字段函数计算, 遵循最左前缀原则。
    3.  **深分页**: `LIMIT 1000000, 10` -> 改为 `ID > last_id LIMIT 10` (游标法) 或 延迟关联。
*   **Q: B+ 树和 B 树的区别？为什么 MySQL 用 B+ 树？**
    *   B+ 树非叶子节点不存数据，只存索引 -> 单页能存更多索引 -> 树更矮 -> IO 次数更少。
    *   B+ 树叶子节点由链表连接 -> 适合范围查询 (Range Scan)。

### 5.2 Elasticsearch / ClickHouse
*   **Q: ES 写入数据为什么会有延迟？(Refresh Interval)**
    *   数据写入 Buffer -> Translog -> 每 1s (默认) Refresh 到 OS Cache (生成 Segment) -> 此时可见。所以是"近实时"。
*   **Q: 倒排索引 (Inverted Index) 原理？**
    *   Term (关键词) -> Document ID List (包含该词的文档列表)。适合全文检索。

---

## 6. 系统设计与软技能

> **JD 强调点**: 容灾容错、高可用(HA)、代码优雅。

*   **Q: 如何设计一个高可用的系统？**
    *   **限流 (Rate Limiting)**: Sentinel/Guava Ratelimiter，防止流量突增。
    *   **熔断 (Circuit Breaker)**: 下游故障时快速失败，防止级联雪崩。
    *   **降级 (Fallback)**: 核心业务优先，非核心业务返回默认值/缓存。
    *   **隔离**: 线程池隔离，服务隔离。
