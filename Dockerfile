# syntax=docker/dockerfile:1

# ==================== Stage 1: Builder ====================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 缓存依赖层（加速构建）
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 复制源码 & 打包（跳过测试以加速）
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==================== Stage 2: Production Runner ====================
FROM eclipse-temurin:21-jre-alpine AS runner

# 创建非 root 用户（安全）
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# JVM 优化参数（适合 2GB 内存 VPS）
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseZGC -XX:+ZGenerational"

EXPOSE 8080

# 健康检查（可选，但推荐）
HEALTHCHECK --interval=30s --timeout=3s CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar