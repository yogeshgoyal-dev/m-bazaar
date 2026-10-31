# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Stage 2: run the jar ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Existing product images so /uploads/** works right after deploy.
COPY uploads ./uploads

# Render free = 512 MB RAM. Keep the JVM small so it does not get killed.
ENV JAVA_TOOL_OPTIONS="-Xmx256m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
