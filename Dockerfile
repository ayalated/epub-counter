FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /counter

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /counter

COPY --from=builder /counter/target/counter-1.0-SNAPSHOT.jar app.jar

EXPOSE 13824

ENTRYPOINT ["java", "-jar", "app.jar"]
