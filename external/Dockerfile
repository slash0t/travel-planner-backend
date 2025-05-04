FROM eclipse-temurin:23-jdk as builder

WORKDIR /app
COPY . .

RUN chmod +x ./mvnw

RUN ./mvnw -pl :external clean package -DskipTests

FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=builder /app/external/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
