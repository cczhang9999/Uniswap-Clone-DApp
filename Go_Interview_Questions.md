# Go Language Interview Questions

## 1. 基础语法 (Basic Syntax)

### 1.1 数组 (Array) 和切片 (Slice) 的区别是什么？
- **数组**: 固定长度，值类型。赋值和传参会复制整个数组。
- **切片**: 动态长度，引用类型（底层结构包含指针、长度、容量）。赋值和传参复制的是切片头（Slice Header），开销小。

### 1.2 `make` 和 `new` 的区别？
- **`new(T)`**: 分配内存，返回 `*T`（零值指针）。适用于值类型如 int, struct。
- **`make(T, args)`**: 分配并初始化，返回 `T`（引用类型本身）。仅用于 slice, map, channel。

### 1.3 Map 是线程安全的吗？
- **不是**。并发读写 Map 会导致 panic (`concurrent map writes`)。
- **解决方案**: 使用 `sync.RWMutex` 加锁，或者使用 `sync.Map`（适用于读多写少场景）。

### 1.4 Defer 的执行顺序？
- **后进先出 (LIFO)**。
- 参数在 `defer` 语句声明时求值（预计算）。
- `defer` 在函数返回前（return 语句之后，真正返回指令之前）执行，可以修改命名返回值。

### 1.5 切片的扩容策略 (Slice Expansion)？
- **Go 1.18+**: 当容量 < 256 时，扩容 2 倍；当容量 >= 256 时，扩容 1.25 倍 (公式更平滑)。
- **内存对齐**: 最终申请的内存容量会根据内存分配器的规格进行向上取整。

### 1.6 Map 的底层实现与负载因子？
- **结构**: 哈希表，使用链地址法解决冲突。由 `hmap` 指向 `bmap` (bucket) 数组。
- **Bucket**: 每个 bucket 存 8 个键值对。溢出时使用 overflow bucket。
- **负载因子 (Load Factor)**: 6.5。超过此值触发扩容（双倍扩容或等量扩容）。

### 1.7 `init()` 函数的执行顺序？
- **包级别**: 依赖包的 `init` -> 当前包的 `const` -> `var` -> `init` -> `main`。
- **多文件**: 同一个包内多个文件的 `init` 执行顺序不保证（通常按文件名排序，但不应依赖）。
- **多次导入**: 一个包被多次导入，其 `init` 只执行一次。

### 1.8 值接收者 vs 指针接收者？
- **指针接收者**: 需要修改接收者、接收者很大（避免复制）、或者包含 `sync.Mutex` 等不可复制字段。
- **值接收者**: 接收者很小（如 int, point）、不需要修改接收者。
- **接口实现**: 指针接收者实现接口，只能用指针赋值给接口；值接收者实现接口，值和指针都可以赋值给接口。

---

## 2. 并发编程 (Concurrency)

### 2.1 Goroutine 和 Thread 的区别？
- **内存占用**: Goroutine 初始栈仅 2KB（可动态伸缩），Thread 通常 1-2MB。
- **调度**: Goroutine 由 Go Runtime (GMP模型) 调度（用户态），开销小；Thread 由 OS 调度（内核态），上下文切换开销大。

### 2.2 GMP 模型是什么？
- **G (Goroutine)**: 任务单元。
- **M (Machine)**: 内核线程，执行 G。
- **P (Processor)**: 逻辑处理器，维护本地运行队列 (Local Run Queue)。
- **机制**: M 必须绑定 P 才能执行 G。P 的本地队列减少了全局锁竞争。支持 Work Stealing（工作窃取）和 Handoff（系统调用阻塞时转移 P）。

### 2.3 Channel 的底层原理？
- 它是线程安全的队列（环形缓冲）。
- **结构**: `hchan` 结构体，包含 `buf` (数组指针), `sendx`, `recvx`, `lock` (Mutex), `sendq`, `recvq` (等待队列)。
- **操作**: 发送/接收时会加锁。缓冲区满/空时，G 会挂起并加入等待队列，由其他 G 唤醒。

### 2.4 Context 的作用？
- 用于在 Goroutine 之间传递取消信号、超时控制和请求作用域数据。
- **常见方法**: `WithCancel`, `WithTimeout`, `WithDeadline`, `WithValue`.

### 2.5 `select` 的用法与随机性？
- 用于处理多个 Channel 操作。
- **随机性**: 如果多个 case 同时满足，`select` 会随机选择一个执行（防止饥饿）。
- **阻塞**: 如果没有 case 满足且没有 default，`select` 会阻塞。

### 2.6 有缓冲 vs 无缓冲 Channel？
- **无缓冲 (Unbuffered)**: 容量为 0。发送和接收必须同步完成（握手），否则阻塞。
- **有缓冲 (Buffered)**: 容量 > 0。缓冲区未满时发送不阻塞，缓冲区非空时接收不阻塞。

### 2.7 `sync.WaitGroup` 的注意事项？
- **Add**: 必须在 Goroutine 启动前调用（避免 Race Condition）。
- **Done**: 在 Goroutine 结束时调用（通常用 defer）。
- **Copy**: `WaitGroup` 包含状态，不可复制（传参需传指针）。

### 2.8 Goroutine 泄漏 (Goroutine Leak)？
- **原因**: Goroutine 启动后无法退出（如阻塞在 Channel 接收/发送、死循环、等待锁）。
- **后果**: 占用内存（栈空间）、导致 OOM、CPU 占用升高。
- **预防**: 确保 Channel 会被关闭或有数据发送；使用 `select` + `ctx.Done()` 处理超时和取消；避免死锁。

### 2.9 Goroutine 中发生 Panic 会怎样？
- **后果**: 如果没有被 `recover` 捕获，会导致**整个进程 (Program)** 崩溃退出，而不仅仅是该 Goroutine 退出。
- **处理**: 在每个启动的 Goroutine 入口处使用 `defer` + `recover` 来捕获可能的 Panic，防止服务挂掉。

### 2.10 如何优雅地停止 Goroutine？
- **Channel**: 发送信号（如关闭 channel）通知 Goroutine 退出。
- **Context**: 使用 `context.WithCancel` 或 `context.WithTimeout`，Goroutine 监听 `ctx.Done()` 信号来退出。

### 2.11 `runtime.GOMAXPROCS` 的作用？
- **作用**: 设置同时执行 Go 代码的操作系统线程（M）的最大数量（即 P 的数量）。
- **默认值**: 机器的 CPU 核心数。
- **调整**: CPU 密集型任务通常设为核心数；IO 密集型任务可以适当调大以提高吞吐量。

### 2.12 Goroutine 的调度时机？
- **主动挂起**: `time.Sleep`, `channel` 读写阻塞, `select` 阻塞, 等待锁 (`sync.Mutex`).
- **系统调用**: 进行系统调用（System Call）时，P 会与 M 分离（Handoff）。
- **协作式调度**: 函数调用时（检查栈扩容标记）、GC 期间。
- **抢占式调度 (Preemptive)**: Go 1.14+ 引入基于信号的异步抢占，防止死循环占用 P 过久（约 10ms）。

---

## 3. 内存管理 (Memory Management)

### 3.1 Go 的垃圾回收 (GC) 算法？
- **三色标记法 (Tri-color Marking)**: 白（未扫描）、灰（待扫描）、黑（已扫描）。
- **混合写屏障 (Hybrid Write Barrier)**: 结合插入写屏障和删除写屏障，允许 GC 和用户代码并发运行，极大地减少了 STW (Stop The World) 时间。

### 3.2 什么是逃逸分析 (Escape Analysis)？
- 编译器决定变量分配在栈上还是堆上。
- **原则**: 如果变量在函数返回后仍被引用（如返回指针、闭包引用、接口动态分派），则逃逸到堆上；否则分配在栈上（随函数返回自动回收）。

---

## 4. 接口 (Interfaces)

### 4.1 接口的底层实现？
- **`eface` (Empty Interface)**: 包含 `_type` (类型信息) 和 `data` (数据指针)。
- **`iface` (Non-empty Interface)**: 包含 `tab` (包含类型信息和方法集) 和 `data`。

### 4.2 Nil Interface 问题？
- 只有当接口的 **类型 (Type)** 和 **值 (Value)** 都为 nil 时，接口才等于 nil。
- 如果接口指向一个 nil 的具体类型指针（如 `var p *int = nil; var i interface{} = p`），此时 `i != nil`。

---

## 6. 错误处理 (Error Handling)

### 6.1 `panic` 和 `recover` 的最佳实践？
- **Panic**: 仅用于不可恢复的严重错误（如数组越界、空指针引用）。业务逻辑错误应返回 `error`。
- **Recover**: 必须在 `defer` 函数中调用。只能捕获当前 Goroutine 的 panic。
- **失效**: 在 `defer` 之外调用 `recover` 无效（返回 nil）。

---

## 5. 进阶话题 (Advanced)

### 5.1 `unsafe.Pointer` 和 `uintptr` 的区别？
- **`unsafe.Pointer`**: 通用指针类型，可以与任意类型指针转换，GC 会追踪它指向的内存。
- **`uintptr`**: 整数类型，仅用于指针运算，GC 不会追踪，可能导致内存被回收。通常配合 `unsafe.Pointer` 使用。

### 5.2 反射 (Reflection) 的性能影响？
- 反射涉及动态类型检查和复杂的内存操作，性能较差。应避免在高性能路径（Hot Path）中使用。
