# Uniswap Clone Backend

This is the Java Spring Boot backend for the Uniswap Clone application.

## Prerequisites

- Java 17 or higher
- Maven (Wrapper included)

## How to Run

You can run the application using the Maven wrapper provided in this directory.

### From the Terminal

Navigate to the `backend` directory and run:

```bash
./mvnw spring-boot:run
```

This command will:
1. Download all necessary dependencies.
2. Compile the code.
3. Start the Spring Boot application.

### From IntelliJ IDEA

If you are using IntelliJ IDEA:
1. Open the `backend` directory as a project or module.
2. Right-click on `pom.xml` and select **Maven > Reload Project**.
3. Locate `src/main/java/com/uniswap/clone/ExchangeApplication.java`.
4. Click the green "Run" arrow next to the `main` method.

## Troubleshooting

### NoClassDefFoundError: org/springframework/boot/SpringApplication

If you see this error, it means the dependencies are not on the classpath. This usually happens if:
- You are running the `java` command directly without a proper classpath.
- The Maven project hasn't been imported correctly in your IDE.

**Solution:** Use `./mvnw spring-boot:run` or re-import the Maven project in your IDE.
