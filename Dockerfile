# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create a slim final image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# คัดลอกเฉพาะไฟล์ .jar ที่ build เสร็จแล้วจาก Stage 1
COPY --from=build /app/target/*.jar app.jar
# Port ที่ Spring Boot application ของคุณรันอยู่ (ปกติคือ 8080)
EXPOSE 8888
ENTRYPOINT ["java","-jar","app.jar"]