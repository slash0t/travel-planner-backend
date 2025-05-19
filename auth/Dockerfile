FROM eclipse-temurin:23-jdk-alpine as builder

WORKDIR /app

COPY external/pom.xml external/
COPY external/.mvn external/.mvn
COPY planner/pom.xml planner/
COPY planner/.mvn planner/.mvn
COPY library/pom.xml library/
COPY library/.mvn library/.mvn

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY .env .

COPY auth auth

RUN chmod +x ./mvnw

RUN ./mvnw -pl :auth clean package -DskipTests

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/auth/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
