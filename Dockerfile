FROM node:26-alpine AS frontend
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:26-jdk AS backend-build
WORKDIR /app
COPY backend/ ./
COPY --from=frontend /app/dist/ src/main/resources/static/
RUN ./gradlew bootJar

FROM eclipse-temurin:26-jre
RUN groupadd -r app && useradd -r -g app app
USER app
COPY --from=backend-build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
