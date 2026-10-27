# 阶段1：编译打包
FROM maven:3.8.8-eclipse-temurin-8 AS builder
WORKDIR /build
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 阶段2：运行
FROM eclipse-temurin:8-jdk
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
# shell模式，支持$PORT变量替换
ENTRYPOINT java -Xmx400m -jar app.jar --server.port=$PORT --spring.profiles.active=prod