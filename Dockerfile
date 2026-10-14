FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/fintrack-backend/fintrack-backend
COPY fintrack-backend/fintrack-backend/pom.xml .
RUN mvn -q dependency:go-offline
COPY fintrack-backend/fintrack-backend/src ./src
COPY fintrack-frontend/fintrack-frontend /workspace/fintrack-frontend/fintrack-frontend
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/fintrack-backend/fintrack-backend/target/fintrack-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
