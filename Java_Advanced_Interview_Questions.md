# Java 高级面试题精选

本文档整理了 Java 高级开发工程师常见的面试题，涵盖 JVM、并发编程、Spring、数据库、Redis 以及分布式系统等核心领域。

## 1. JVM (Java Virtual Machine)

### 1.1 内存模型 (JMM)
- **问题**: 请简述 JVM 的内存区域划分（运行时数据区）。
- **要点**: 堆 (Heap), 栈 (Stack), 方法区 (Method Area/Metaspace), 程序计数器 (PC Register), 本地方法栈 (Native Method Stack)。重点区分线程私有和线程共享区域。

### 1.2 垃圾回收 (GC)
- **问题**: 常见的垃圾回收算法有哪些？CMS 和 G1 收集器的区别是什么？
- **要点**:
    - 算法: 标记-清除, 标记-复制, 标记-整理, 分代收集。
    - CMS: 以获取最短回收停顿时间为目标，基于“标记-清除”，容易产生内存碎片。
    - G1: 面向服务端，基于 Region 内存布局，可预测停顿时间，整体基于“标记-整理”，局部基于“复制”。

### 1.3 类加载机制
- **问题**: 什么是双亲委派模型？为什么要打破双亲委派模型？
- **要点**: Bootstrap -> Extension -> Application ClassLoader。打破场景：Tomcat Web容器隔离、JDBC Driver加载 (SPI)。

### 1.4 调优
- **问题**: 生产环境 CPU 飙高或 OOM 如何排查？
- **要点**: `top` 命令定位进程 -> `top -Hp` 定位线程 -> `jstack` 导出线程栈分析（死锁/死循环）。OOM 使用 `jmap` dump 堆内存 -> MAT/VisualVM 分析对象引用。


### 1.5 内存泄露 (Memory Leak)
- **问题**: Java 中会存在内存泄露吗？请举例说明。
- **要点**:
    - **定义**: 对象不再被使用，但 GC 无法回收。
    - **场景**:
        - **静态集合**: `static List/Map` 长期持有对象引用。
        - **未关闭资源**: 数据库连接、IO 流、Socket 未 close。
        - **ThreadLocal**: Key 弱引用被回收，Value 强引用未 remove。
        - **内部类**: 非静态内部类隐式持有外部类引用。

## 2. 并发编程 (Concurrency)

### 2.1 线程池
- **问题**: `ThreadPoolExecutor` 的核心参数有哪些？线程池的工作流程是怎样的？
- **要点**: `corePoolSize`, `maximumPoolSize`, `keepAliveTime`, `workQueue`, `threadFactory`, `handler` (拒绝策略)。流程：核心线程 -> 队列 -> 最大线程 -> 拒绝。

### 2.2 锁机制
- **问题**: `synchronized` 和 `ReentrantLock` 的区别？什么是 CAS 和 AQS？
- **要点**:
    - `synchronized`: 关键字，JVM 层实现，自动释放，支持锁升级（偏向->轻量->重量）。
    - `ReentrantLock`: API 层实现，需手动释放，支持公平锁、中断、超时。
    - CAS (Compare And Swap): 乐观锁基石，ABA 问题。
    - AQS (AbstractQueuedSynchronizer): 锁的基础框架，基于 volatile state 和 FIFO 队列。

### 2.3 ThreadLocal
- **问题**: `ThreadLocal` 的原理是什么？为什么会内存泄漏？
- **要点**: 每个线程维护一个 `ThreadLocalMap`。Key 是弱引用，Value 是强引用。如果 ThreadLocal 没有被回收，且线程复用（线程池），Value 无法回收导致泄漏。务必 `remove()`。

**start() vs run()**: `start()` 启动新线程 (异步)；`run()` 只是普通方法调用 (同步)。
*   **wait() vs sleep()**:
    *   `wait()`: Object 类，释放锁，需在同步块中。
    *   `sleep()`: Thread 类，不释放锁。

### 2.4 死锁 (Deadlock)
- **问题**: 什么是死锁？产生死锁的四个必要条件？如何避免？
- **要点**:
    - **定义**: 两个或多个线程互相持有对方需要的资源，导致永久等待。
    - **四个条件**: 互斥、占有且等待、不可强占、循环等待。
    - **避免**:
        - 破坏循环等待: 按顺序加锁 (如 A->B)。
        - 破坏占有且等待: 一次性申请所有资源。
        - 使用 `tryLock` (带超时机制)。

## 3. Spring Framework

### 3.1 IOC & AOP
- **问题**: Spring Bean 的生命周期？AOP 的实现原理（JDK Proxy vs CGLIB）？
- **要点**: 实例化 -> 属性赋值 -> 初始化 (Aware, PostProcessor, init-method) -> 使用 -> 销毁。AOP 动态代理：接口使用 JDK，类使用 CGLIB。

### 3.2 事务
- **问题**: Spring 事务的传播行为 (Propagation) 有哪些？
- **要点**:
    - **REQUIRED** (默认): 如果当前有事务，加入该事务；如果没有，新建一个。
    - **REQUIRES_NEW**: 挂起当前事务，新建一个事务（独立提交/回滚）。
    - **NESTED**: 嵌套事务（Savepoint），父事务回滚子事务也回滚，子事务回滚父事务可不回滚。
    - **SUPPORTS**: 有事务就加入，没有就非事务执行。

- **问题**: Spring 事务失效的常见场景？
- **要点**:
    - **访问权限**: 方法不是 `public`。
    - **自调用**: 类内部方法调用 (`this.method()`)，绕过了 AOP 代理。
    - **异常处理**: 异常被 `try-catch` 吞掉，未抛出。
    - **异常类型**: 默认只回滚 `RuntimeException` 和 `Error`，Checked Exception 需要指定 `rollbackFor`。
    - **数据库**: 数据库引擎不支持事务 (如 MyISAM)。

- **问题**: Spring 事务是如何实现的？
- **要点**: 基于 AOP (动态代理)。在目标方法执行前开启事务 (setAutoCommit(false))，执行后提交或回滚。

### 3.3 循环依赖
- **问题**: Spring 如何解决循环依赖？
- **要点**: 三级缓存 (`singletonObjects`, `earlySingletonObjects`, `singletonFactories`)。构造器注入无法解决。

## 4. MySQL 数据库

### 4.1 索引
- **问题**: B+ 树和 B 树的区别？为什么 MySQL 选择 B+ 树？聚簇索引和非聚簇索引？
- **要点**: B+ 树叶子节点存数据且链表连接，适合范围查询；非叶子节点只存索引，层高更低。聚簇索引：数据和索引在一起（主键）；非聚簇索引：叶子节点存主键值（回表）。

### 4.2 事务隔离级别
- **问题**: MySQL 的隔离级别？MVCC (多版本并发控制) 是如何实现的？
- **要点**: 读未提交, 读已提交 (RC), 可重复读 (RR, 默认), 串行化。MVCC 基于 Undo Log 版本链和 Read View。

### 4.3 锁
- **问题**: 什么是间隙锁 (Gap Lock)？如何解决幻读？
- **要点**: RR 级别下，Next-Key Lock (Record Lock + Gap Lock) 解决幻读。

### 4.4 慢 SQL 优化
- **问题**: 发现慢 SQL 如何排查？Explain 关键字关注哪些字段？
- **要点**:
    - **排查**: 开启慢查询日志 (slow_query_log), 使用 `mysqldumpslow` 分析 top SQL。
    - **Explain**:
        - `type`: 访问类型，优劣顺序 system > const > eq_ref > ref > range > index > ALL (全表扫描)。至少要达到 range 级别。
        - `key`: 实际使用的索引。
        - `rows`: 扫描行数。
        - `Extra`: `Using filesort` (需要文件排序, 差), `Using temporary` (需要临时表, 差), `Using index` (覆盖索引, 好)。

- **问题**: SQL 优化有哪些常见手段？
- **要点**:
    - **索引优化**: 最左前缀法则，避免索引失效（如对字段计算、使用 `!=`、`OR` 连接非索引字段、`LIKE '%abc'`）。
    - **SQL 语句**: 避免 `SELECT *`，使用覆盖索引；`limit` 分页优化（子查询/ID限定）；`join` 尽量用小表驱动大表。
    - **架构**: 读写分离，分库分表。

## 5. Redis 缓存


*   **数据类型**: String, List, Set, Hash, ZSet。
*   **分布式锁**: Redisson (Watchdog 自动续期)。
*   **缓存问题**:
    *   **雪崩**: 大量 Key 同时失效 -> 随机过期时间。
    *   **击穿**: 热点 Key 失效 -> 互斥锁。
    *   **穿透**: 查询不存在的数据 -> 布隆过滤器, 缓存空对象。
*   **集群**: Hash 取模, 一致性 Hash, Hash Slot (Redis Cluster)。

### 5.1 数据结构与场景
- **问题**: Redis 常用数据类型及应用场景？String, Hash, List, Set, ZSet。
- **要点**: 计数器 (String), 购物车 (Hash), 消息队列 (List), 抽奖/点赞 (Set), 排行榜 (ZSet)。

### 5.2 持久化
- **问题**: RDB 和 AOF 的区别？
- **要点**: RDB (快照, 恢复快, 丢数据多), AOF (追加日志, 数据全, 文件大, 恢复慢)。通常混合使用。

### 5.3 缓存问题
- **问题**: 什么是缓存穿透、击穿、雪崩？解决方案？
- **要点**:
    - 穿透: 查不存在的数据 -> 布隆过滤器/空值缓存。
    - 击穿: 热点 Key 过期 -> 互斥锁/逻辑过期。
    - 雪崩: 大量 Key 同时过期 -> 随机过期时间/高可用集群。

### 5.4 分布式锁
- **问题**: 实现分布式锁有哪些常见的方案？各自的优缺点？
- **要点**:
    - **Redis**: 性能高，AP 模型，可能丢锁（主从切换时）。实现简单 (`setnx`, Redisson)。
    - **Zookeeper**: 可靠性高，CP 模型，强一致性。性能不如 Redis，基于临时顺序节点 + Watch 机制。
    - **数据库**: 性能差，不推荐。基于唯一索引或排他锁。

- **问题**: Redis 分布式锁如何实现？`setnx` 有什么问题？
- **要点**:
    - 早期: `setnx` + `expire` 分两步执行，非原子性，可能死锁。
    - 改进: `set key value EX seconds NX` (原子命令)。
    - 缺陷: 锁过期但业务没跑完（锁失效）；主从异步复制导致锁丢失。

- **问题**: Redisson 是如何解决锁过期问题的？(Watchdog 机制)
- **要点**:
    - **Watchdog (看门狗)**: 默认每 10s (lockWatchdogTimeout/3) 检查一次，如果线程还持有锁，就自动续期 (默认 30s)。
    - **可重入性**: 使用 Hash 结构存储 (Key, ThreadId, Count)，支持同一线程多次加锁。

- **问题**: 什么是 Redlock 算法？
- **要点**: 为了解决 Redis 主从切换丢锁问题。客户端向 N 个独立的 Redis 节点依次申请锁，超过半数 (N/2 + 1) 成功且耗时小于 TTL 才算成功。但在实际工程中争议较大（依赖时钟），一般推荐用 Zookeeper 替代 Redlock 追求强一致性。

- **问题**: 分布式锁的“羊群效应”是什么？如何避免？
- **要点**: Zookeeper 中如果所有客户端都监听同一个节点，锁释放时会唤醒所有客户端，造成网络风暴。
- **解决**: 客户端只监听自己前一个顺序节点 (临时顺序节点)，形成链式监听。

### 5.5 哨兵模式 (Sentinel)
- **问题**: 什么是 Redis 哨兵模式？它解决了什么问题？
- **要点**:
    - **定义**: Redis 高可用 (HA) 解决方案。
    - **核心功能**:
        - **监控 (Monitoring)**: 不断检查 Master 和 Slave 是否正常运行。
        - **通知 (Notification)**: 实例异常时，通过 API 通知管理员。
        - **自动故障转移 (Automatic Failover)**: Master 挂了，自动将一个 Slave 提升为 Master。
        - **配置提供者 (Configuration Provider)**: 客户端连接 Sentinel 获取 Master 地址。

- **问题**: 哨兵是如何判断 Master 下线的？(主观下线 vs 客观下线)
- **要点**:
    - **主观下线 (SDOWN)**: 单个 Sentinel 节点在 `down-after-milliseconds` 时间内未收到 Master 的有效回复，认为 Master 挂了。
    - **客观下线 (ODOWN)**: 多个 Sentinel (超过 Quorum 数量) 都认为 Master 挂了，才判定为客观下线，触发故障转移。

- **问题**: 哨兵的 Leader 选举与故障转移流程？
- **要点**:
    1.  **选举 Leader**: 发现 Master 客观下线的 Sentinel 会申请成为 Leader (基于 Raft 算法)，获得多数票者当选。
    2.  **选新 Master**: Leader 从 Slaves 中选出一个作为新 Master (筛选条件：在线、网络好、优先级高、复制偏移量大)。
    3.  **切换**: 让其他 Slaves 复制新 Master；旧 Master 上线后变为 Slave。

- **问题**: 什么是脑裂 (Split Brain)？如何解决？
- **要点**:
    - **现象**: 网络分区导致出现两个 Master，客户端可能向旧 Master 写入数据，恢复后数据丢失。
    - **解决**: 配置 `min-slaves-to-write` (最少从节点数) 和 `min-slaves-max-lag` (最大延迟)，不满足条件时拒绝写入。

## 6. 消息队列 (Message Queue)

### 6.1 消息队列的作用
- **问题**: 为什么使用消息队列？有什么优缺点？
- **要点**:
    - **优点**: 解耦 (Decoupling), 削峰 (Peak Shaving), 异步 (Asynchronous)。
    - **缺点**: 系统可用性降低 (MQ 挂了怎么办), 复杂性增加 (消息丢失、重复消费、顺序性), 一致性问题。

### 6.2 消息丢失解决方案
- **问题**: 如何保证消息不丢失？(以 RabbitMQ/RocketMQ 为例)
- **要点**:
    - **生产端**: 开启 Confirm 模式 / 事务机制。
    - **MQ 服务端**: 开启持久化 (Exchange, Queue, Message 都要持久化)；集群模式下开启镜像队列 / 同步刷盘。
    - **消费端**: 关闭自动 ACK，业务处理完成后手动 ACK。

### 6.3 消息重复消费
- **问题**: 如何保证消息不被重复消费 (幂等性)？
- **要点**:
    - 数据库唯一索引 (Insert ignore)。对于已经消费成功的消息，本地数据库表或Redis缓存业务标识，每次处理前先进行校验，保证幂等。
    - Redis Set/ZSet 防重。
    - 状态机 (Status Machine) CAS 更新。
    - 乐观锁 (Version)。

### 6.4 消息顺序性
- **问题**: 如何保证消息的顺序性？
- **要点**:
    - **RabbitMQ**: 拆分多个 Queue，每个 Queue 对应一个 Consumer。
    - **RocketMQ/Kafka**: 保证同一个 OrderId 发送到同一个 Queue/Partition (Hash取模)，且 Consumer 单线程消费。

### 6.5 消息积压
- **问题**: 线上消息积压几百万条怎么办？
- **要点**:
    - 临时扩容: 修复 Consumer Bug，新建 Topic (Partition 是原来的 10 倍)，写临时程序将积压消息转发到新 Topic，多 Consumer 并发消费。
    - 预案: 降级非核心业务，丢弃非重要消息。
### 7.1 场景与选型
*   **场景**: 削峰填谷, 异步处理, 解耦。
*   **选型**: Kafka (高吞吐), RocketMQ (可靠性, 事务), RabbitMQ (低延迟)。

### 7.2 核心问题
*   **消息丢失**: 生产端 (Ack), 服务端 (持久化/副本), 消费端 (手动 Ack)。
*   **重复消费**: 幂等性设计 (数据库唯一键, Redis 记录)。
*   **顺序消息**: 局部有序 (同一 Key 发往同一分区，单线程消费)。
*   **消息堆积**: 增加消费者，优化消费逻辑，临时扩容。
*   **事务消息**: 半消息 + 二次确认 (RocketMQ)。

## 7. 分布式系统 / 微服务

### 7.1 CAP & BASE
- **问题**: CAP 定理是什么？为什么不能同时满足？
- **要点**: Consistency (一致性), Availability (可用性), Partition tolerance (分区容错性)。P 是必须的，只能在 C 和 A 之间权衡。

### 7.2 分布式事务
- **问题**: 常见的分布式事务解决方案？
- **要点**: 2PC (Seata AT), TCC, 本地消息表, 消息事务 (RocketMQ), 最大努力通知。

### 7.3 服务治理
- **问题**: 注册中心 (Nacos/Eureka) 的原理？负载均衡 (Ribbon) 策略？熔断降级 (Sentinel/Hystrix)？
- **要点**: 心跳机制、服务拉取。轮询、随机、权重。

### 7.4 API 网关 (Gateway)
- **问题**: 为什么需要 API 网关？它有哪些核心功能？
- **要点**:
    - **统一入口**: 路由转发 (Routing)，屏蔽内部微服务细节。
    - **安全认证**: 统一鉴权 (Authentication/Authorization)，黑白名单。
    - **流量控制**: 限流 (Rate Limiting)，熔断降级。
    - **日志监控**: 统一日志记录，链路追踪。

- **问题**: Spring Cloud Gateway 和 Zuul 的区别？
- **要点**:
    - **Zuul 1.x**: 基于 Servlet 阻塞 I/O，线程池模式，性能一般。
    - **Spring Cloud Gateway**: 基于 Spring WebFlux (Netty + Reactor)，非阻塞异步 I/O，性能更强，功能更丰富。

- **问题**: Gateway 的断言 (Predicate) 和过滤器 (Filter) 是什么？
- **要点**:
    - **Predicate**: 路由匹配条件 (Path, Method, Header, Host 等)。
    - **Filter**: 请求处理逻辑 (AddHeader, StripPrefix, RateLimit 等)。分为 Pre (前置) 和 Post (后置) 过滤。

### 7.5 高可用设计：限流、熔断、降级
- **问题**: 限流、熔断、降级有什么区别？它们是如何实现的？
- **要点**:
    - **核心区别**:
        - **限流 (Rate Limiting)**: **流量进来时**控制，保护**自己**不被上游请求打垮。
        - **熔断 (Circuit Breaking)**: **调用出去时**控制，保护**自己**不被下游服务拖垮（防止雪崩）。
        - **降级 (Degradation)**: 整体资源不足时，牺牲非核心业务，保核心业务。

    - **1. 限流 (Rate Limiting)**
        - **算法**:
            - **计数器 (Fixed Window)**: 单位时间（如1s）计数，超过阈值拒绝。缺点：临界突发流量（如0.9s和1.1s各来100请求，1s窗口内没超，但200ms内来了200请求）。
            - **滑动窗口 (Sliding Window)**: 将时间窗口细分（如1s分10格），解决临界问题。
            - **漏桶 (Leaky Bucket)**: 水（请求）先进桶，桶底以**恒定速率**流出。**强行削峰**，无法处理突发流量。
            - **令牌桶 (Token Bucket)**: 以恒定速率往桶里放令牌，请求拿令牌。**允许突发流量**（只要桶里有存货）。
        - **实现**:
            - **单机**: Guava `RateLimiter` (基于令牌桶)。
            - **分布式**: Redis + Lua (原子性操作计数), Sentinel, Nginx (`limit_req_zone`).

    - **2. 熔断 (Circuit Breaking)**
        - **原理**: 类似电路保险丝。
        - **状态机 (State Machine)**:
            - **Closed (关闭)**: 正常状态，所有请求放行。
            - **Open (打开)**: 当失败率（或响应时间）超过阈值，熔断器打开，后续请求**直接失败**（不走网络调用），避免等待。
            - **Half-Open (半开)**: 熔断一段时间后（休眠窗口），允许**少量**请求通过。如果成功，恢复 Closed；如果失败，继续 Open。
        - **组件**: Hystrix (Netflix, 停止更新), Resilience4j (轻量级), Sentinel (阿里, 支持流量整形).

    - **3. 降级 (Degradation)**
        - **触发条件**: 熔断开启、接口超时、CPU/内存过高、人工干预（大促关闭非核心服务）。
        - **策略**:
            - **返回默认值**: `return null` 或 `return 0`。
            - **兜底数据**: 返回缓存中的旧数据，或者静态提示文案。
            - **静默处理**: 记录日志后直接忽略。

### 7.6 Dubbo 框架
- **问题**: Dubbo 是什么？解决了什么问题？
- **要点**:
    - **定义**: 阿里开源的高性能 RPC 框架，现为 Apache 顶级项目。
    - **解决的问题**:
        - **服务化拆分**: 将单体应用拆分为微服务，实现服务间的远程调用。
        - **服务治理**: 提供服务注册、发现、负载均衡、容错、监控等完整的服务治理能力。
    - **核心架构**:
        - **Provider (服务提供者)**: 暴露服务。
        - **Consumer (服务消费者)**: 调用远程服务。
        - **Registry (注册中心)**: 服务注册与发现（Zookeeper, Nacos）。
        - **Monitor (监控中心)**: 统计服务调用次数和耗时。
        - **Container (容器)**: 服务运行容器。

- **问题**: Dubbo 的服务注册与发现流程？
- **要点**:
    1. **Provider 启动**: 向 Registry 注册自己提供的服务。
    2. **Consumer 启动**: 向 Registry 订阅所需的服务。
    3. **Registry 返回**: Provider 列表给 Consumer，Consumer 缓存本地。
    4. **Consumer 调用**: 基于负载均衡算法选择一个 Provider 发起调用。
    5. **异步通知**: Provider 变更时，Registry 推送变更给 Consumer。
    6. **Monitor 统计**: Consumer 和 Provider 定时发送统计数据到 Monitor。

- **问题**: Dubbo 支持哪些负载均衡策略？
- **要点**:
    - **Random (随机)**: 默认策略，按权重随机选择。适合服务性能差异不大的场景。
    - **RoundRobin (轮询)**: 按权重轮询。适合服务性能相近的场景。
    - **LeastActive (最少活跃)**: 优先调用活跃数最少的 Provider（慢的 Provider 收到更少请求）。适合性能差异大的场景。
    - **ConsistentHash (一致性哈希)**: 相同参数的请求总是发到同一 Provider。适合有状态服务。

- **问题**: Dubbo 的集群容错策略有哪些？
- **要点**:
    - **Failover (失败自动切换)**: 默认策略，失败后重试其他 Provider。适合读操作。
    - **Failfast (快速失败)**: 只调用一次，失败立即报错。适合非幂等写操作。
    - **Failsafe (失败安全)**: 失败后忽略异常，返回空结果。适合日志记录等不重要操作。
    - **Failback (失败自动恢复)**: 失败后记录请求，定时重发。适合消息通知。
    - **Forking (并行调用)**: 同时调用多个 Provider，只要一个成功即返回。适合实时性要求高的读操作。
    - **Broadcast (广播调用)**: 逐个调用所有 Provider，任意一个失败则失败。适合通知所有 Provider 更新缓存。

- **问题**: Dubbo SPI 和 Java SPI 的区别？
- **要点**:
    - **Java SPI**:
        - 一次性加载所有实现类，浪费资源。
        - 不支持按需加载和依赖注入。
    - **Dubbo SPI**:
        - **按需加载**: 只加载需要的扩展实现。
        - **自适应扩展 (Adaptive)**: 根据 URL 参数动态选择扩展实现。
        - **依赖注入 (IOC)**: 支持扩展点之间的依赖注入。
        - **AOP**: 支持扩展点的 Wrapper 包装（类似装饰器模式）。

- **问题**: Dubbo 支持哪些序列化方式？如何选择？
- **要点**:
    - **Hessian**: 默认，跨语言，性能一般，兼容性好。
    - **Kryo**: 性能高，但不支持跨语言。
    - **Protobuf**: Google 出品，性能高，需要定义 IDL。
    - **JSON**: 可读性好，性能差，适合调试。
    - **选择建议**: 内网高性能场景用 Kryo；跨语言场景用 Protobuf；默认用 Hessian。

- **问题**: Dubbo 如何实现服务降级？
- **要点**:
    - **Mock 机制**: 在 Consumer 端配置 `mock="return null"` 或自定义 Mock 类。
    - **触发场景**:
        - 服务调用失败（超时、异常）时返回 Mock 数据。
        - 人工配置强制降级（通过 Dubbo Admin）。
    - **应用**: 大促时降级非核心服务，返回默认值或缓存数据。

- **问题**: Dubbo 的异步调用是如何实现的？
- **要点**:
    - **原理**: 基于 Netty 的 NIO 异步通信 + Future/CompletableFuture。
    - **配置方式**:
        - `@DubboReference(async = true)`: 异步调用。
        - 通过 `RpcContext.getContext().getFuture()` 获取 Future。
    - **优势**: 不阻塞调用线程，提高吞吐量。适合调用多个服务并行处理的场景。

## 7.7. Java 基础
    *   **String**: final 修饰，不可继承，不可变。
*   **StringBuilder vs StringBuffer**: Builder 非线程安全 (快)；Buffer 线程安全 (慢)。
*   **List vs Map**: List 有序可重复；Map 键值对，Key 唯一。
*   **ArrayList vs Vector**: ArrayList 非线程安全；Vector 线程安全 (synchronized)。
*   **HashMap vs Hashtable**: HashMap 非线程安全，允许 null；Hashtable 线程安全，不允许 null。
*   **HashMap 原理 (JDK 1.8)**: 数组 + 链表 + 红黑树。链表长度 > 8 转红黑树，< 6 转链表。扩容阈值 0.75。
*   **== vs equals**: `==` 比地址；`equals` 比内容 (需重写)。
*   **final**: 修饰类 (不可继承)、方法 (不可重写)、变量 (常量)。

## 10. MyBatis
*   **核心**: SqlSessionFactory, SqlSession, Mapper。
*   **#{} vs ${}**: `#{}` 预编译 (防注入); `${}` 直接拼接。
*   **缓存**: 一级缓存 (SqlSession 级别, 默认开启); 二级缓存 (Mapper 级别, 需配置)。
*   **动态 SQL**: `if`, `choose`, `where`, `foreach`。
*   **插件原理**: 动态代理 (拦截 Executor, ParameterHandler, ResultSetHandler, StatementHandler)。


### 8.2 项目案例：体育商户分组系统
*   **业务**: 不同商户 (c, s, y, b) 进入对应场馆，注单分流到对应数据库。
*   **数据流**: 业务库 -> FlinkSQL -> 汇总库/报表库。
*   **技术栈**: Spring Boot, Redis + Ehcache (二级缓存), 配置中心, 服务治理。
*   **缓存策略**: 避免缓存 Key 同时失效导致雪崩 (设置随机过期时间)。
### 4.3 分库分表
*   **垂直切分**: 按列拆分 (大字段独立)。
*   **水平切分**: 按行拆分 (分库分表, 时间切分)。
*   **中间件**: Mycat, ShardingSphere。


请举一个你处理过的棘手技术问题的例子。

候选人 (您)： “好的。在之前的项目中，我曾遇到过一个缓存雪崩的问题。

发生的背景 (Situation): 当时系统在高峰期突然响应变慢，数据库 CPU 飙升到 90% 以上。
分析 (Analyze): 我第一时间查看监控面板，发现 Redis 的 QPS 突然下降，而数据库 QPS 激增。经过排查日志，发现是大量热门赛事的赔率缓存 Key 设置了相同的过期时间，导致在同一时刻集体失效。
解决 (Action):
短期: 我紧急对数据库进行了限流保护，防止宕机。
长期: 我优化了缓存策略。第一，在设置过期时间时加上一个随机值（比如 5-10 分钟的随机波动），避免同时失效；第二，引入了 Ehcache 本地缓存作为二级缓存，当 Redis 不可用或未命中时，先查本地缓存，减少数据库压力。
结果 (Result): 上线后，数据库的负载在高峰期下降了 60%，系统再也没有出现过类似的雪崩情况。事后我也输出了关于缓存设计的规范文档。”

## 面试注意事项 (Interview Tips)

### 1. 面试前准备 (Preparation)
*   **简历熟悉度**: 对简历上的每一个项目、每一个技术点都要了如指掌。被问到简历内容答不上来是**大忌**。
*   **自我介绍**: 准备 1-3 分钟的自我介绍，包含：个人基本信息 + 核心技术栈 + 亮点项目经验 + 个人优势。
*   **基础扎实**: 常见八股文（JVM, 并发, MySQL, Redis, Spring）要滚瓜烂熟，这是敲门砖。

### 2. 面试中技巧 (During Interview)
*   **沟通 (Communication)**:
    *   **听清问题**: 没听清可以礼貌询问 "不好意思，您是指...吗？"。
    *   **条理清晰**: 回答问题分点阐述（第一、第二、第三），不要逻辑混乱。
    *   **STAR 法则**: 讲项目经验时遵循 **S**ituation (背景), **T**ask (任务), **A**ction (行动/方案), **R**esult (结果/数据)。
*   **技术深度 (Depth)**:
    *   **知其所以然**: 不要只背八股文，要多谈 "为什么" (Why) 和 "原理" (How)。
    *   **引导话题**: 尽量把问题引导到自己熟悉的领域。
    *   **承认不足**: 遇到真不会的，可以坦诚 "这个细节我暂时不太清楚，但我了解相关的..."，展示解决问题的思路比硬凑答案更好。
*   **态度 (Attitude)**:
    *   **自信**: 声音洪亮，眼神交流。
    *   **谦逊**: 即使面试官错了，也不要激烈争辩，可以委婉表达 "我的理解是..."。

### 3. 面试后 (After Interview)
*   **反问环节**: 面试官问 "你有什么想问我的吗？"
    *   **推荐问**: "咱们团队目前主要的技术栈是什么？"、"您对这个岗位的期望是什么？"、"团队目前面临的最大技术挑战是什么？"
    *   **不推荐问**: "薪资多少？" (HR 面才问)、"我有通过吗？"、"太简单/太傻的问题"。


解决：每次拉取的时间窗口向前回溯。
例如：上次拉取到 T，本次拉取范围应为 [T - buffer_time, Now]。
buffer_time 可以设为 1-5 分钟，取决于数据库事务最大延迟。
注意：这会导致重复拉取，因此必须在处理端做**幂等性（Idempotency）**校验（通过 Order ID 去重）


### 5.5 线上实战问题排查

#### Q1: 遇到过 POI 导出导致内存溢出 (OOM) 吗？怎么解决的？
*   **原因**：Apache POI 的默认 `XSSFWorkbook` 会将整个 Excel DOM 加载到内存。数据量大（如 >5w 行）时，对象极多，导致 Heap 空间不足。
*   **解决方案**：
    1.  **使用 SXSSF (Streaming API)**：
        *   使用 `SXSSFWorkbook` 替代 `XSSFWorkbook`。
        *   **原理**：利用滑动窗口（Sliding Window），默认只在内存保留 100 行，其余行写入磁盘临时文件。
        *   **效果**：内存占用极低，不再随数据量线性增长。
    2.  **使用 EasyExcel (阿里开源)**：
        *   **原理**：基于 SAX 的事件驱动模型解析，一行一行处理，不一次性加载。
        *   **优势**：更省内存，API 更简单，避免了 POI 的繁琐操作。

#### Q2: 线上 CPU 飙高或频繁 Full GC 怎么排查？
*   **排查套路**：
    1.  **Top 命令**：`top` 找进程 PID -> `top -Hp PID` 找线程 TID -> `printf "%x" TID` 转 16 进制。
    2.  **Jstack 定位**：`jstack PID | grep <16进制TID> -A 20` 查看该线程在干嘛。
        *   如果是 `VM Thread`，说明是 GC 在忙。
        *   如果是业务线程，看具体卡在哪个方法。
    3.  **Jstat 观察 GC**：`jstat -gcutil PID 1000`。
        *   如果 `FGC` (Full GC 次数) 飙升，`Old` (老年代) 占用率居高不下，说明是内存泄漏或大对象。
    4.  **Dump 分析**：`jmap -dump:format=b,file=dump.hprof PID`，用 MAT 或 VisualVM 分析，找出占用内存最大的对象。

#### Q3: 怎么排查和解决死锁 (Deadlock)？
*   **现象**：程序无响应，不报错，日志停止滚动。
*   **排查**：
    *   直接使用 `jstack <PID>`。
    *   JVM 会在输出末尾自动打印 `Found one Java-level deadlock`，并列出涉及的线程和锁。
*   **解决**：
    *   找到代码位置，分析锁获取顺序。
    *   **破局**：保证所有线程**以相同的顺序加锁**（例如按 ID 从小到大加锁），或者使用 `Lock.tryLock(timeout)` 避免无限等待。


     ### 1.6 JVM 调优实战

#### Q1: 说说你是如何进行 JVM 调优的？（方法论）
*   **核心观点**: 
    1.  **大多数情况下不需要调优**，JDK 8+ 的默认配置已经很优秀。
    2.  调优的目标通常是：**降低延迟** (减少 STW 时间) 或 **提高吞吐量**。
*   **调优步骤**:
    1.  **监控 (Monitor)**: 结合 Prometheus + Grafana 或 `jstat` 监控 GC 频率和耗时。
        *   *警报线*: Minor GC 频繁（如每秒一次），Full GC 频繁（如几分钟一次，正常应几天一次）。
    2.  **分析 (Analyze)**: 
        *   如果是 Full GC 频繁，分析是 **内存泄漏** 还是 **老年代空间不足**。
        *   如果是 Minor GC 频繁，看是否 **新生代太小**。
    3.  **调整 (Tune)**: 修改 JVM 参数（见下文）。
    4.  **验证 (Verify)**: 灰度发布，对比调优前后的 GC 指标和接口响应时间 (RT)。

#### Q2: 生产环境常用的 JVM 参数有哪些？
*   **堆内存设置**:
    *   `-Xms4g -Xmx4g`: 设置初始堆和最大堆大小。**技巧：通常设为相同**，避免 JVM 在运行时动态扩容/缩容产生的性能开销（通常设为物理内存的 60%-80%）。
*   **新生代设置**:
    *   `-Xmn2g`: 设置新生代大小。**技巧：通常设为堆内存的 1/3 到 1/2**。
        *   *太大*: 老年代变小，容易 Full GC；Minor GC 单次耗时变长。
        *   *太小*: Minor GC 极其频繁，对象来不及回收就晋升到老年代，导致老年代填满触发 Full GC。
*   **元空间 (Metaspace)**:
    *   `-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m`: 避免元空间动态扩容。
*   **垃圾收集器**:
    *   `-XX:+UseG1GC`: 推荐 JDK 8+ 大堆（>4G）使用 G1。
    *   `-XX:MaxGCPauseMillis=200`: (G1专用) 设定目标停顿时间为 200ms，G1 会尽力满足。
*   **Dump 配置 (必配)**:
    *   `-XX:+HeapDumpOnOutOfMemoryError`: OOM 时自动生成堆转储文件。
    *   `-XX:HeapDumpPath=/data/logs/`: 指定 Dump 文件路径。

#### Q3: 讲一个你做过的 JVM 调优案例？（实战话术）
*   **背景**: "之前的项目高峰期，我们发现核心用户获取账号信息服务在高峰期偶尔会出现接口响应尖刺（Spike），耗时超过 1 秒。"
*   **分析**: 
    "我查看 GC 日志，发现 **Full GC 大约每半小时发生一次**。进一步分析发现，是因为新生代设置得比较小（默认比例 1:2），导致大量短生命周期的订单对象在 Minor GC 时无法被回收（Survivor 区放不下），被迫提前晋升到老年代。"
*   **解决**: 
    "我调整了参数，将新生代大小 (`-Xmn`) 扩大了 50%（从 1G 调到 1.5G），同时使用了 G1 收集器并设置最大停顿时间为 100ms。"
*   **结果**: 
    "上线后，Minor GC 频率略微降低，但 **Full GC 变成了几天才发生一次**，接口响应非常平稳，再没有出现过尖刺。"


    ### 2.2.1 synchronized 详解

#### Q1: synchronized 的底层原理是什么？
*   **字节码层面**:
    *   **代码块**: 使用 `monitorenter` 和 `monitorexit` 指令。
    *   **方法**: 使用 `ACC_SYNCHRONIZED` 标志。
*   **JVM 层面 (Monitor)**:
    *   每个对象都关联一个 Monitor (监视器锁)。
    *   当线程尝试获取锁时，会尝试成为 Monitor 的 Owner。
    *   Monitor 内部维护了 `EntryList` (等待锁的线程) 和 `WaitSet` (调用 wait 被阻塞的线程)。

#### Q2: 什么是锁升级 (Lock Coarsening)？
*   **背景**: JDK 1.6 之前 synchronized 是重量级锁，性能差。1.6 之后引入了锁升级机制，根据竞争情况动态调整锁的状态。
*   **升级过程**:
    1.  **偏向锁 (Biased Lock)**: 只有一个线程访问锁。锁对象头记录该线程 ID，下次该线程再来无需同步。
    2.  **轻量级锁 (Lightweight Lock)**: 有第二个线程来竞争（无锁竞争）。通过 CAS 自旋尝试获取锁。
    3.  **重量级锁 (Heavyweight Lock)**: 竞争激烈（CAS 自旋失败或多个线程竞争）。阻塞线程，进入操作系统内核态，性能开销大。
*   **注意**: 锁只能升级，不能降级。

#### Q3: synchronized 和 ReentrantLock 的核心区别？
| 特性 | synchronized | ReentrantLock |
| :--- | :--- | :--- |
| **实现层面** | JVM 关键字 (C++ 实现) | JDK API (Java 实现) |
| **锁释放** | 自动释放 (异常也释放) | 必须手动 `unlock()` (通常在 finally 中) |
| **锁类型** | 非公平锁 | 默认非公平，可设为公平锁 |
| **等待可中断** | 不支持 | 支持 (`lockInterruptibly`) |
| **条件变量** | 单一 Condition (`wait/notify`) | 支持多个 Condition (`newCondition`) |


## 8. Netty 与网络编程

### 8.1 IO 模型
- **问题**: BIO, NIO, AIO 的区别？
- **要点**:
    - **BIO (Blocking IO)**: 同步阻塞。一个连接一个线程，并发能力低。
    - **NIO (Non-blocking IO)**: 同步非阻塞。基于 Selector (多路复用器)，一个线程处理多个连接 (Channel)。
    - **AIO (Asynchronous IO)**: 异步非阻塞。基于回调机制，操作系统完成后通知应用。

### 8.2 Netty 核心
- **问题**: 为什么选择 Netty 而不是原生 NIO？Netty 的高性能体现在哪？
- **要点**:
    - **API 易用**: 解决了 NIO 繁琐的 Selector 操作和 Bug (如 Epoll 空轮询)。
    - **零拷贝 (Zero Copy)**: 使用 `DirectBuffer` (堆外内存) 和 `FileRegion` (sendfile)，减少内核态到用户态的数据拷贝。
    - **Reactor 模型**: 主从 Reactor 多线程模型 (BossGroup 接收连接, WorkerGroup 处理读写)。
    - **对象池**: Recycler 重用对象，减少 GC。

- **问题**: 什么是 TCP 粘包/拆包？Netty 怎么解决？
- **要点**:
    - **原因**: TCP 是流式协议，没有消息边界。
    - **解决**:
        - **定长**: `FixedLengthFrameDecoder`。
        - **分隔符**: `DelimiterBasedFrameDecoder`。
        - **长度字段**: `LengthFieldBasedFrameDecoder` (最常用，消息头包含长度)。


## 9. 设计模式 (Design Patterns)

### 9.1 常用模式
- **单例模式 (Singleton)**: 双重检查锁 (DCL) + volatile。
- **工厂模式 (Factory)**: Spring IOC 容器就是大工厂。解耦对象的创建与使用。
- **代理模式 (Proxy)**: Spring AOP, RPC 远程调用。
- **策略模式 (Strategy)**: 替代大量的 `if-else`。例如：支付渠道 (AliPay, WeChatPay) 实现同一接口，根据 Context 选择策略。
- **模板方法 (Template Method)**: `JdbcTemplate`, `RedisTemplate`。定义流程骨架，子类实现细节。
- **观察者模式 (Observer)**: Spring Event (`ApplicationListener`), Zookeeper Watcher。

### 9.2 实战题
- **问题**: 你在项目中用过哪些设计模式？
- **话术**: "在重构用户模块时，我使用了**策略模式**来处理不同的用户登录场馆，避免了大量的 `if-else` 判断。同时使用**工厂模式**配合 Spring 的 `Map<String, Service>` 自动注入来获取对应的策略实例，使代码扩展性极强，新增渠道只需加一个类即可。"

## 11. 高频系统设计题

### 11.1 分布式 ID 生成
- **问题**: 如何生成全局唯一的 ID？
- **方案**:
    1.  **UUID**: 简单，但无序、太长、影响索引性能。
    2.  **数据库自增**: 性能瓶颈，依赖 DB。
    3.  **Redis incr**: 性能高，需维护 Redis 高可用。
    4.  **雪花算法 (Snowflake)**: 推荐。
        - **结构**: 1位符号 + 41位时间戳 + 10位机器ID + 12位序列号。
        - **优点**: 本地生成 (高性能)，趋势递增 (对索引友好)。
        - **缺点**: 依赖机器时钟 (时钟回拨问题)。

### 11.2 秒杀系统设计
- **核心挑战**: 瞬时高并发，防止超卖，防止拖垮下游。
- **设计要点**:
    1.  **前端**: 按钮置灰，静态资源 CDN 缓存。
    2.  **网关**: 限流 (Rate Limiting)，黑名单拦截。
    3.  **缓存**: Redis 预减库存 (Lua 脚本保证原子性)。
    4.  **MQ**: 削峰填谷，异步下单。
    5.  **数据库**: 乐观锁 (`update stock set num = num - 1 where num > 0`)。

  ## 12. Flink & 实时数仓 (Real-time Data Warehouse)

### 12.1 FlinkSQL 数据同步 (CDC)
- **问题**: 如何使用 FlinkSQL 实现 MySQL 到其他存储（如 ES, Kafka, Doris）的实时同步？
- **要点**:
    - **CDC (Change Data Capture)**: 使用 `flink-connector-mysql-cdc` 组件，底层基于 Debezium 监听 MySQL Binlog。
    - **流程**:
        1.  **Source**: 定义 MySQL CDC 表 (`CREATE TABLE source_table ... WITH ('connector' = 'mysql-cdc'...)`)。
        2.  **Sink**: 定义目标表 (如 Elasticsearch, Kafka)。
        3.  **Sync**: 执行 `INSERT INTO sink_table SELECT * FROM source_table`。
    - **优势**: 支持**全量+增量**自动切换，无需锁表；支持**断点续传**。

### 12.2 核心机制
- **问题**: Flink 如何保证端到端的 Exactly-Once (精确一次)？
- **要点**:
    - **内部**: 依赖 **Checkpoint** (基于 Chandy-Lamport 算法的分布式快照) 保存状态。
    - **端到端**: 
        - **Source**: 支持数据重放 (如 Kafka Offset)。
        - **Sink**: 支持事务写入 (如 Kafka 的事务, MySQL 的 XA 事务) 或 幂等性写入。
        - **机制**: **二阶段提交 (2PC)**。Checkpoint 完成时，协调 Sink 提交事务。

- **问题**: 什么是反压 (Backpressure)？如何排查和解决？
- **要点**:
    - **现象**: 下游消费速度 < 上游生产速度，导致数据堆积在网络 Buffer 中，Checkpoint 可能会超时失败。
    - **排查**: Flink Web UI -> Backpressure 面板 (显示 High/Normal)。
    - **解决**:
        - **增加并发**: 提高算子并行度 (Parallelism)。
        - **优化 Sink**: 开启批量写入 (Batch Flush)，优化数据库性能。
        - **开启 MiniBatch**: 减少状态访问次数。

### 12.3 常见问题
- **问题**: FlinkSQL 遇到数据倾斜怎么办？
- **要点**:
    - **表现**: 某个并行度处理的数据量远超其他，导致单点延迟。
    - **解决**:
        - **LocalGlobal 聚合**: 开启 `table.exec.mini-batch.enabled` 和 `table.exec.mini-batch.allow-latency`，类似 MapReduce 的 Combiner 预聚合。
        - **Split Distinct**: 将 `COUNT(DISTINCT key)` 拆分为两层聚合。
        - **加盐 (Salt)**: 在 Key 上拼接随机前缀打散数据，聚合后再去掉前缀。  