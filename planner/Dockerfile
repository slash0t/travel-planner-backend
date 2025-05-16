FROM eclipse-temurin:23-jdk-alpine as builder

WORKDIR /app

COPY external/pom.xml external/
COPY external/.mvn external/.mvn
COPY auth/pom.xml auth/
COPY auth/.mvn auth/.mvn
COPY library/pom.xml library/
COPY library/.mvn library/.mvn

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY .env .

COPY planner planner

RUN chmod +x ./mvnw

RUN ./mvnw -pl :planner clean package -DskipTests

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/planner/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]