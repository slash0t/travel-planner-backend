FROM eclipse-temurin:23-jdk-alpine as builder

WORKDIR /app

COPY auth/pom.xml auth/
COPY auth/.mvn auth/.mvn
COPY planner/pom.xml planner/
COPY planner/.mvn planner/.mvn
COPY external/pom.xml external/
COPY external/.mvn external/.mvn

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY .env .

COPY library library

RUN chmod +x ./mvnw

RUN ./mvnw -pl :library clean package -DskipTests

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/library/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
