# React 高级面试题精选

本文档整理了 React 开发中常见的面试题，涵盖核心原理、Hooks、状态管理、性能优化以及 Next.js 相关知识。

## 1. 核心原理

### 1.1 什么是 Virtual DOM？为什么要使用它？
- **概念**: 它是真实 DOM 的内存表示（JavaScript 对象树）。
- **作用**: 
    - **性能**: 减少直接操作真实 DOM 的次数（DOM 操作昂贵）。通过 Diff 算法计算出最小变更，批量更新。
    - **跨平台**: Virtual DOM 本质是 JS 对象，可以渲染到 Web (ReactDOM)、Native (ReactNative) 等不同平台。

### 1.2 React Diff 算法的策略？
- **同层比较**: 只比较同一层级的节点，不跨层级比较。
- **类型判断**: 如果组件类型不同（如 `div` 变 `span`），直接销毁原树，重建新树。
- **Key 的作用**: 在列表渲染中，利用 `key` 标识节点，复用已有节点，避免不必要的销毁和重建。

### 1.3 什么是 Fiber 架构？解决了什么问题？
- **问题**: 在 React 15 中，更新过程是同步且不可中断的。如果组件树很大，主线程被长期占用，会导致页面卡顿（掉帧）。
- **Fiber**: React 16 引入的新的协调引擎。它将更新任务拆分成一个个小的单元（Fiber 节点），利用浏览器空闲时间（RequestIdleCallback）分片执行。
- **特性**: 可中断、可恢复、优先级调度。

## 2. Hooks 与组件

### 2.1 为什么引入 Hooks？
- **复用逻辑**: 解决了类组件中 HOC (高阶组件) 和 Render Props 导致的“嵌套地狱”问题，自定义 Hooks 让逻辑复用更简单。
- **代码组织**: 将相关的逻辑（如订阅/取消订阅）放在一起，而不是分散在生命周期方法中。
- **this 指向**: 避免了类组件中复杂的 `this` 绑定问题。

### 2.2 常用的 Hooks 及其作用
- `useState`: 状态管理。
- `useEffect`: 处理副作用（API 调用、订阅、DOM 操作）。相当于 `componentDidMount` + `componentDidUpdate` + `componentWillUnmount`。
- `useContext`: 跨组件层级共享数据。
- `useMemo`: 缓存计算结果，性能优化。
- `useCallback`: 缓存函数引用，避免子组件不必要的重渲染。
- `useRef`: 访问 DOM 节点或保存可变变量（不触发重渲染）。

### 2.3 useEffect 的依赖数组 `[]`
- **不传**: 每次渲染后都执行。
- **空数组 `[]`**: 只在组件挂载（Mount）时执行一次，卸载（Unmount）时执行清理函数。
- **传变量 `[dep]`**: 当依赖变量发生变化时执行。

## 3. 状态管理

### 3.1 组件间通信方式
- **父传子**: Props。
- **子传父**: 回调函数。
- **跨层级**: Context API。
- **全局状态**: Redux, MobX, Zustand, Recoil。

### 3.2 Redux 的工作流程
1.  **Action**: 描述发生了什么（纯对象）。
2.  **Dispatch**: 发送 Action。
3.  **Reducer**: 根据 Action 更新 State（纯函数，不可变数据）。
4.  **Store**: 存储 State，通知 View 更新。

## 4. 性能优化

### 4.1 如何避免不必要的渲染？
- **React.memo**: 用于函数组件，浅比较 Props，如果未变则不重渲染。
- **useMemo / useCallback**: 缓存复杂计算结果和函数引用，防止 Props 变化导致子组件重渲染。
- **Key**: 列表渲染确保 Key 的唯一性和稳定性（不要用 index）。

### 4.2 代码分割 (Code Splitting)
- 使用 `React.lazy` 和 `Suspense` 实现组件的懒加载，减小首屏 Bundle 体积。

## 5. React 18 新特性

### 5.1 Concurrent Mode (并发模式)
允许 React 同时准备多个版本的 UI，使渲染过程可中断，保持页面响应。

### 5.2 Automatic Batching (自动批处理)
在 React 18 中，Promise、setTimeout、原生事件处理函数中的多次状态更新也会被自动合并为一次渲染，提高性能。

## 6. Next.js (SSR/SSG)

### 6.1 渲染模式区别
- **CSR (客户端渲染)**: 浏览器下载 JS 后渲染，首屏慢，SEO 差。
- **SSR (服务端渲染)**: `getServerSideProps`。服务器生成 HTML 返回，首屏快，SEO 好，但服务器压力大。
- **SSG (静态生成)**: `getStaticProps`。构建时生成 HTML，性能最好，适合内容不常变的页面。
- **ISR (增量静态再生)**: 允许在运行时更新静态页面。

### 6.2 Next.js 的优势
- 开箱即用的 SSR/SSG。
- 自动文件路由 (Pages Router / App Router)。
- API Routes (自带后端能力)。
- 图片优化 (`next/image`)。
