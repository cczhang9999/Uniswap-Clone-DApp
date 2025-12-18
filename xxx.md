Spring 事务机制和 注解 是面试中非常高频的考点，尤其是涉及到原理和失效场景。以下为您整理的 Spring 事务机制核心面试题详解：
1. Spring 事务的核心接口有哪些？
Spring 的事务管理是基于接口设计的，核心接口是 
PlatformTransactionManager。

* PlatformTransactionManager: 事务管理器核心接口，提供 getTransaction(), commit(), rollback() 方法。
    * 常见实现：DataSourceTransactionManager (MyBatis/JDBC), JpaTransactionManager (JPA), JtaTransactionManager (分布式事物 JTA)。
* TransactionDefinition: 定义事务属性（隔离级别、传播行为、超时、只读）。
* TransactionStatus: 事务运行状态（是否由新事务启动、是否有保存点、是否已完成）。
2. Spring 事务的隔离级别 (Isolation Level) 有哪些？
Spring 默认使用数据库的隔离级别 (
ISOLATION_DEFAULT)，但也支持手动指定：

* READ_UNCOMMITTED: 读未提交，可能脏读。
* READ_COMMITTED: 读已提交，防止脏读，可能不可重复读（Oracle/SQL Server 默认）。
* REPEATABLE_READ: 可重复读，防止脏读和不可重复读（MySQL 默认）。
* SERIALIZABLE: 串行化，最高级别，性能最低。
3. Spring 事务的传播行为 (Propagation) 有哪些？ (重点)
Spring 定义了 7 种传播行为，用于解决业务方法相互调用时事务如何处理的问题：
* REQUIRED (默认): 如果当前存在事务，则加入该事务；如果不存在，则创建一个新事务。 (最常用)
* REQUIRES_NEW: 挂起当前事务，创建一个新的独立事务。 (常用于记录日志、发送消息等不论主业务成败都要执行的操作)
* NESTED: 如果当前存在事务，则在嵌套事务内执行 (Savepoint)；如果不存在，则行为类似 REQUIRED。 (父事务回滚子事务必回滚，但子事务回滚父事务可 catch 不回滚)
* SUPPORTS: 支持当前事务，如果不存在事务，则以非事务方式执行。 (常用于读操作)
* MANDATORY: 必须在一个已存在的事务中执行，否则抛出异常。
* NOT_SUPPORTED: 以非事务方式执行，如果当前存在事务，则挂起当前事务。
* NEVER: 以非事务方式执行，如果当前存在事务，则抛出异常。
4. @Transactional 注解的实现原理是什么？ (核心)
基于 Spring AOP (面向切面编程) 实现：
1. 动态代理: Spring 在启动时会为带有 @Transactional 注解的 Bean 创建代理对象 (CGLIB 或 JDK Dynamic Proxy)。
2. 拦截器: 当调用代理对象的方法时，会被 TransactionInterceptor 拦截。
3. 事务开启: 在目标方法执行前，通过 TransactionManager 开启事务 (获取数据库连接，设置 setAutoCommit(false), 绑定连接到当前线程 ThreadLocal)。
4. 执行目标方法: 执行实际的业务逻辑。
5. 提交/回滚:
    * 如果方法正常返回，提交事务。
    * 如果捕获到异常 (默认是 RuntimeException 或 Error)，回滚事务。
5. Spring 事务在哪些场景下会失效？ (避坑/排查)
这是面试中最常问的“坑”：
1. 方法内部自调用 (Self-Invocation): 在同一个类中，一个非事务方法调用同类的事务方法。因为直接使用 this 调用绕过了 Spring 的 AOP 代理，导致切面逻辑未执行。
    * 解决: 注入自身 Bean 调用，或使用 AopContext.currentProxy()。
2. 方法权限问题: @Transactional 应用在 private, protected, package-private 方法上默认无效（CGLIB/JDK 代理要求）。
3. 异常被吞掉: 业务代码自己 try-catch 捕获了异常且没有抛出，Spring 认为方法执行成功从而提交事务。
    * 解决: catch 块中手动抛出 throw new RuntimeException(e) 或 TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()。
4. 异常类型不匹配: 默认只回滚 RuntimeException。如果抛出 Checked Exception (如 IOException) 且未配置 rollbackFor，事务不会回滚。
    * 解决: @Transactional(rollbackFor = Exception.class)。
5. 事务管理器未配置: 多数据源场景下，未指定正确的 transactionManager。
6. Spring 事务和数据库事务的区别？
* 数据库事务: 数据库引擎 (InnoDB) 本身提供的 ACID 保证。
* Spring 事务: 是对底层数据库事务的抽象和封装。Spring 不具备事务能力，它只是负责“由谁来管理连接”、“何时开启/提交/回滚”，最终还是调用数据库连接的 commit/rollback。



Spring 框架的注解非常丰富，面试中通常考察分类记忆和具体场景的使用细节。以下是为您整理的 Spring 高频常用注解，按功能分类：
1. 声明 Bean (IOC 容器注册)
这些注解用于将类标识为 Spring 容器管理的 Bean。
* @Component: 通用的 Bean 声明组件，如果没有明确的角色，就用这个。
* @Service: 对应业务层 (Service Layer)。通常在此层标注并开启事务。
* @Repository: 对应数据访问层 (DAO Layer)。Spring 会自动处理其抛出的 SQL 异常，转换为 Spring 的 DataAccessException。
* @Controller: 对应表现层 (Web MVC)。标记为控制器，处理 HTTP 请求。
* @Configuration: 声明当前类是一个配置类，相当于 XML 配置文件。通常配合 @Bean 使用。
* @Bean: 用在方法上，告诉 Spring 这个方法的返回值也是一个 Bean，Bean ID 默认是方法名。
2. 依赖注入 (DI)
* @Autowired: Spring 原生注解，默认按类型 (byType) 装配。如果想按名称，需配合 @Qualifier("beanName")。
* @Resource: Java 标准注解 (JSR-250)，默认按名称 (byName) 装配。推荐使用，因为它更通用，减少与 Spring 的耦合。
* @Value: 注入普通属性值（如 String, int）或读取配置文件 (.properties / .yml) 中的值，如 @Value("${server.port}")。
* @Qualifier: 当通过接口注入时，如果有多个实现类，用此注解指定具体的 Bean 名称。
3. Spring MVC / Web 开发
* @RestController: 组合注解，相当于 @Controller + @ResponseBody。表示该控制器所有方法都返回 JSON/XML 数据，而不是视图页面。
* @RequestMapping: 映射 HTTP 请求路径。可用于类或方法上。
* @GetMapping / @PostMapping / @PutMapping / @DeleteMapping: 简化的 @RequestMapping，分别对应 GET, POST, PUT, DELETE 请求。
* @RequestBody: 将 HTTP 请求体 (JSON) 绑定到对象上。
* @ResponseBody: 将返回值直接写入 HTTP 响应体 (JSON)，不走视图解析器。
* @PathVariable: 获取 URL 路径变量，如 /user/{id} 中的   id。
* @RequestParam: 获取 URL 查询参数，如 /user?id=1 中的   id。
* @RequestHeader: 获取请求头信息。
4. Spring Boot 核心
* @SpringBootApplication: 核心启动注解，包含 @SpringBootConfiguration, @EnableAutoConfiguration, @ComponentScan。
* @EnableAutoConfiguration: 开启自动配置，Spring Boot 的黑魔法核心，根据 classpath 依赖自动配置 Bean。
* @ConfigurationProperties: 将配置文件 (application.yml) 中的属性绑定到 Java Bean 上，通常配合 @Component 使用。
5. AOP 与 事务
* @Transactional: 声明式事务管理。可加在类（对所有 public 方法生效）或方法上。
* @Aspect: 声明一个切面类。
* @Pointcut: 定义切点（拦截哪些方法）。
* @Before, @After, @Around, @AfterThrowing: 定义通知（Advice），即在切点前后执行的逻辑。
6. 全局异常处理
* @ControllerAdvice / @RestControllerAdvice: 全局异常处理类。
* @ExceptionHandler: 在方法上声明，用于捕获特定的异常类，并返回自定义的错误响应。
7. 生命周期与作用域
* @PostConstruct: 在 Bean 构造并注入属性后执行，用于初始化。
* @PreDestroy: 在 Bean 销毁前执行，用于释放资源。
* @Scope: 定义 Bean 的作用域，如 "singleton" (默认, 单例), "prototype" (多例), "request", "session"。
面试高频对比题
1. @Component 和 @Bean 的区别？
    * @Component 作用于类，是通过类路径扫描自动检测并注册。
    * @Bean 作用于配置类的方法，通常用于引入第三方库的组件，或者需要复杂初始化逻辑的场景。
2. @Autowired 和 @Resource 的区别？
    * @Autowired 是 Spring 的注解，默认按类型；@Resource 是 Java EE 的注解，默认按名称。建议用 @Resource 减少对 Spring 框架的依赖。
3. @Controller 和 @RestController 的区别？
    * @RestController = @Controller + @ResponseBody。前者专用于 RESTful API，后者用于传统的 MVC 页面跳转。
