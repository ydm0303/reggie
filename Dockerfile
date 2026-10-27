# 阶段1：编译打包
FROM maven:3.8.8-eclipse-temurin-8 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 阶段2：运行
FROM eclipse-temurin:8-jdk
WORKDIR /app
# 从构建阶段复制jar
COPY --from=builder /build/target/*.jar app.jar
# 启动命令，激活prod环境
ENTRYPOINT ["java","-Xmx400m","-jar","app.jar","--server.port=$PORT","--spring.profiles.active=prod"]
