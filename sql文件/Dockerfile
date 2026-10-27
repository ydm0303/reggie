FROM openjdk:8-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Xmx400m","-jar","app.jar","--server.port=$PORT"]
