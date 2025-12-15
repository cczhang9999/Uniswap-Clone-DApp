# Go Language Interview Questions

这份文档整理了 Go (Golang) 后端开发面试中常见的问题，涵盖了从基础语法到高级并发、运行时原理以及系统设计等多个维度。

---

## 1. 基础语法与数据类型 (Basic Syntax & Data Types)

### 1.1 数组 (Array) 和切片 (Slice) 的核心区别？
- **数组**: 值类型，固定长度。作为函数参数传递时会发生**完整复制**，开销大。
- **切片**: 引用类型，动态长度。底层结构是一个 `SliceHeader`，包含三个字段：
  ```go
  type SliceHeader struct {
      Data uintptr // 指向底层数组的指针
      Len  int     // 当前长度
      Cap  int     // 当前容量
  }
  ```
  传递切片时只复制这三个字段，开销极小。

### 1.2 `make` 和 `new` 的区别？
- **`new(T)`**: 为类型 `T` 分配零值内存，返回 **指针** `*T`。适用于 `int`, `struct` 等值类型。
- **`make(T, args)`**: 专门用于分配并初始化 **引用类型** (slice, map, channel)。返回 **T 本身**（非指针），因为这三种类型底层已经包含了引用。

### 1.3 `defer` 的执行顺序与陷阱？
- **顺序**: 后进先出 (LIFO)，类似于栈。
- **参数预计算**: `defer` 函数的参数在声明时就会被计算并锁定，而不是在执行时。
- **修改返回值**: `defer` 在 `return` 语句赋值之后、函数真正返回之前执行。因此，`defer` 可以修改 **命名返回值 (Named Return Values)**。

### 1.4 `for range` 的坑？
- **循环变量复用**: 在 Go 1.22 之前，`for k, v := range` 中的 `v` 变量地址是固定的，每次循环只更新其值。如果在循环中启动 Goroutine 并直接使用 `v`，会导致所有 Goroutine 看到的都是同一个最后的值。（Go 1.22 已修复此问题，每次循环都会创建新变量）。

---

## 2. 数据结构底层 (Data Structures Deep Dive)

### 2.1 Map 的底层实现与扩容机制？
- **底层**: 哈希表 (`hmap`)。buckets 数组指针指向 `bmap` (bucket) 结构。
- **Bucket**: 每个 bucket 存 8 个 Key-Value 对（为了内存对齐，key 和 value 分开存放）。
- **Hash 冲突**: 使用链地址法，bucket 满了会连接 overflow bucket。
- **扩容**:
  - **负载因子 > 6.5**: 触发**双倍扩容** (Same Size Grow)，渐进式迁移数据。
  - **Overflow bucket 过多**: 触发**等量扩容** (Same Size Grow，实际上是整理内存)，减少碎片。
- **并发安全**: Map **不是** 线程安全的。并发读写会 Panic (`concurrent map writes`)。解决方案：
  1. `sync.RWMutex`
  2. `sync.Map` (适合读多写少)
  3. 分段锁 (Concurrent Map)

### 2.2 切片 (Slice) 的扩容策略？
- **Go 1.18+**:
  - `Cap < 256`: 双倍扩容 (`2x`).
  - `Cap >= 256`: 使用公式 `newCap = oldCap + (oldCap + 3*256) / 4` 平滑过渡，增长率从 2.0 逐渐降低到 1.25。
- **内存对齐**: 最终申请的内存大小会根据 Go 内存分配器的 span class 进行向上取整，可能略大于计算值。

---

## 3. 并发编程 (Concurrency) - **重点**

### 3.1 GMP 调度模型详解？
- **G (Goroutine)**: 用户态线程，包含栈、指令指针等。初始栈仅 2KB。
- **M (Machine)**: 内核线程 (OS Thread)，实际执行代码的实体。
- **P (Processor)**: 逻辑处理器，维护了一个本地运行队列 (Local Run Queue)。默认数量 = CPU 核心数。
- **调度策略**:
  - **本地队列**: 减少全局锁竞争。
  - **Global Queue**: 本地队列满了放全局，M 如果本地没活干会去全局拿。
  - **Work Stealing**: M 吃完自己 P 的 G，会去偷其他 P 的 G。
  - **Handoff (系统调用)**: 当 M 阻塞在系统调用时，P 会脱离 M，寻找新的 M 来继续执行队列中的 G。

### 3.2 Channel 的底层实现与状态
- **结构**: `hchan`，包含环形缓冲 `buf`、互斥锁 `lock`、发送/接收队列 `recvq`/`sendq`。
- **Panic 场景**:
  - 向已关闭的 Channel 发送数据。
  - 关闭一个已经关闭的 Channel。
- **阻塞场景**:
  - 读/写 `nil` Channel 会**永久阻塞**。
- **优雅关闭**: 只有发送方应该关闭 Channel。

### 3.3 Context 的使用场景与原理？
- **作用**: 传递取消信号、超时控制、Trace ID 等请求域数据。
- **原理**: 树状结构。父 Context 取消 `cancel()`，会递归调用所有子 Context 的 `cancel()`，关闭子 Context 中的 `Done` channel，从而通知子 Goroutine 退出。
- **方法**: `WithCancel`, `WithTimeout`, `WithDeadline`, `WithValue`.

### 3.4 `sync.Map` 适合什么场景？
- 只有在 **读多写少** (Key 稳定，只会更新 Value) 或者 **各 Goroutine 操作的 Key 集合不重叠** 时，性能才优于 `Mutex + Map`。
- 因为 `sync.Map` 用了空间换时间（read map 和 dirty map 两份数据），写操作涉及到 dirty map 的加锁和 promote，写入性能并不好。

### 3.5 什么是数据竞争 (Data Race)？如何检测？
- **定义**: 多个 Goroutine 同时访问同一块内存，且至少有一个是写操作。
- **检测**: 运行时使用 `go run -race` 或 `go test -race` 开启 Race Detector。建议在 CI/CD 流程中强制开启。

---

## 4. 内存管理与垃圾回收 (Memory & GC)

### 4.1 Go GC 的演进与三色标记法？
- **算法**: 并发三色标记清除 (Concurrent Tri-color Mark-Sweep)。
- **三色**:
  - 白: 潜在垃圾。
  - 灰: 活跃对象，但子对象未扫描。
  - 黑: 活跃对象，子对象已扫描。
- **写屏障 (Write Barrier)**:
  - 为了允许 GC 和用户代码并发运行，Go 使用了 **混合写屏障 (Hybrid Write Barrier)** (Go 1.8+)。
  - 核心思想：在 GC 进行时，任何新创建或修改的对象引用都被视为“活跃”，防止在这个过程中对象被误回收。这极大地缩短了 STW (Stop The World) 时间（通常 < 1ms）。

### 4.2 逃逸分析 (Escape Analysis)？
- **栈 vs 堆**: 栈内存分配快（指针移动），堆内存需要 GC 回收。
- **逃逸**: 编译器分析变量的作用域。如果变量在函数返回后还被外部引用（例如返回指针），它就会“逃逸”到堆上。
- **优化**: 尽量减少逃逸，可以减轻 GC 压力。

### 4.3 内存泄漏 (Memory Leak) 常见原因？
- **Goroutine 泄漏**: Goroutine 阻塞在永远不会有数据的 Channel 上，导致无法退出，栈空间无法释放。
- **长切片引用短切片**: 大数组的切片引用，导致底层大数组无法被回收。
- **不再使用的 `time.Ticker`**: 未调用 `Stop()`。

---

## 5. 接口与反射 (Interface & Reflection)

### 5.1 接口的 nil 判断陷阱？
- 接口由 `(Type, Value)` 两个部分组成。
- 只有当 `Type` 和 `Value` **都为 nil** 时，`interface == nil` 才成立。
- **陷阱**: 一个具体的 `*int` 指针是 `nil`，赋值给 `interface{}` 后，该接口的 `Value` 是 `nil`，但 `Type` 是 `*int`，所以接口 **不等于 nil**。

### 5.2 什么是鸭子类型 (Duck Typing)？
- "If it walks like a duck and quacks like a duck, it's a duck."
- Go 的接口是 **隐式实现** 的。这是 Go 对解耦的极致体现。

---

## 6. 系统设计与工程实践 (System Design & Engineering)

### 6.1 如何设计一个高并发的限流器 (Rate Limiter)？
- **计数器**: 简单，但有临界突发流量问题。
- **滑动窗口**: 解决临界问题，精度取决于窗口颗粒度。
- **漏桶 (Leaky Bucket)**: 固定流出速率，适合平滑流量（削峰填谷）。
- **令牌桶 (Token Bucket)**: 固定速率放入令牌，支持一定程度的突发流量（只要桶里有令牌）。**官方库**: `golang.org/x/time/rate` 实现了令牌桶。

### 6.2 分布式 ID 生成方案？
- **UUID**: 简单但太长，无序，影响数据库索引性能。
- **Snowflake (雪花算法)**: 64位整数，包含 时间戳 + 机器ID + 序列号。趋势递增，性能极高。
- **数据库号段模式**: 依赖 DB，批量获取 ID 段。

### 6.3 常见的 Go 性能优化手段？
- **复用对象**: 使用 `sync.Pool` 减少堆内存分配，减轻 GC 压力。
- **预分配内存**: Slice 和 Map 初始化时指定容量 `make([]int, 0, 100)`，避免多次扩容。
- **减少锁竞争**: 减小锁粒度，使用原子操作 `atomic` 替代锁，使用 Channel 串行化访问。
- **字符串拼接**: 使用 `strings.Builder` 代替 `+`。

### 6.4 单元测试与基准测试？
- **单元测试**: `TestXxx(*testing.T)`。
- **基准测试 (Benchmark)**: `BenchmarkXxx(*testing.B)`，用于测试函数性能。
- **Mock**: 使用 `gomock` 或 `testify/mock` 模拟依赖接口。

### 6.5 Net/HTTP 标准库是异步的吗？
- **不是**传统的异步 I/O (如 Node.js)。
- Go 的 HTTP Server 对**每个请求启动一个 Goroutine** (`go c.serve(ctx)`).
- 但由于 Go Runtime 的 I/O 多路复用 (epoll/kqueue) 机制，这种同步写法的底层 I/O 是非阻塞的，性能非常高，且代码逻辑比回调地狱清晰得多。
