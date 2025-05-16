FROM eclipse-temurin:23-jdk-alpine as builder

WORKDIR /app

COPY auth/pom.xml auth/
COPY auth/.mvn auth/.mvn
COPY planner/pom.xml planner/
COPY planner/.mvn planner/.mvn
COPY library/pom.xml library/
COPY library/.mvn library/.mvn

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY .env .

COPY external external

RUN chmod +x ./mvnw

RUN ./mvnw -pl :external clean package -DskipTests

FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=builder /app/external/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
