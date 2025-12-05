package main

import (
	"context"
	"fmt"
	"math/rand"
	"sync"
	"time"
)

// fetchData 模拟网络请求
func fetchData(url string, wg *sync.WaitGroup) {
	// 最佳实践：wg 应该由调用者管理，但如果在这里处理，需确保不为 nil
	if wg != nil {
		defer wg.Done()
	}

	// 模拟随机延迟 (0-1000ms)
	delay := time.Duration(rand.Intn(1000)) * time.Millisecond
	time.Sleep(delay)

	fmt.Printf("Fetch: %s | Delay: %v\n", url, delay)
}

// worker 模拟后台工作者
// 优化：使用 Ticker 代替 default+Sleep，避免忙等待和不可控的调度
func worker(ctx context.Context, id int, wg *sync.WaitGroup) {
	defer wg.Done()

	// 创建一个打点器，每 500ms 触发一次任务
	ticker := time.NewTicker(500 * time.Millisecond)
	defer ticker.Stop() // 确保释放资源

	fmt.Printf("Worker %d started\n", id)

	for {
		select {
		case <-ctx.Done():
			// 接收到取消信号（超时或主动取消）
			fmt.Printf("Worker %d: Stopped (Reason: %v)\n", id, ctx.Err())
			return

		case <-ticker.C:
			// 模拟执行周期性任务
			// 注意：这里不需要 time.After 来做超时，因为 Ticker 本身就是定时的
			// 如果需要模拟任务执行耗时：
			workTime := time.Duration(rand.Intn(300)) * time.Millisecond
			fmt.Printf("Worker %d: Working... (takes %v)\n", id, workTime)
			time.Sleep(workTime)
		}
	}
}

func main() {
	rand.Seed(time.Now().UnixNano()) // Go 1.20+ 之前需要手动 Seed，为了兼容性加上

	urls := []string{
		"http://www.baidu.com",
		"http://www.baidu.com",
		"http://www.baidu.com",
		"http://www.baidu.com",
	}

	// === 1. 串行执行 (Serial) ===
	fmt.Println("=== 1. Serial Execution ===")
	start := time.Now()
	for _, url := range urls {
		fetchData(url, nil)
	}
	fmt.Printf("Serial Total time: %d ms\n\n", time.Since(start).Milliseconds())

	// === 2. 并发执行 (Concurrent) ===
	fmt.Println("=== 2. Concurrent Execution ===")
	start1 := time.Now()
	var wg sync.WaitGroup

	for _, url := range urls {
		wg.Add(1)
		go fetchData(url, &wg)
	}

	wg.Wait()
	fmt.Printf("Concurrent Total time: %d ms\n\n", time.Since(start1).Milliseconds())

	// === 3. Context 控制 Worker ===
	fmt.Println("=== 3. Context Worker Control ===")
	// 设置 3 秒超时
	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
	defer cancel() // 最佳实践：即使是超时 Context，也应该调用 cancel 释放资源

	var workerWg sync.WaitGroup
	
	// 启动 2 个 Worker
	for i := 1; i <= 2; i++ {
		workerWg.Add(1)
		go worker(ctx, i, &workerWg)
	}

	// 等待所有 Worker 结束
	// 相比于原本的 time.Sleep(5 * time.Second)，使用 WaitGroup 更精确
	workerWg.Wait()
	fmt.Println("All workers finished.")
}
