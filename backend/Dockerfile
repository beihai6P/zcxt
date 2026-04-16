FROM maven:3.9.10-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml /app/pom.xml
RUN mvn -q -B -DskipTests=true dependency:go-offline
COPY backend/src /app/src
RUN mvn -q -B -DskipTests=true package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/backend-0.1.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

