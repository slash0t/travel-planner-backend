FROM eclipse-temurin:23-jdk as builder

WORKDIR /app
COPY .. .

RUN ./mvnw -pl :auth clean package -DskipTests

FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=builder /app/auth/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
