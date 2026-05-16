# ---------- Build Stage ----------
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

# Copy Maven wrapper + pom first
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Give permission
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source
COPY src src

# Build jar
RUN ./mvnw clean package -DskipTests


# ---------- Run Stage ----------
FROM eclipse-temurin:26-jre

WORKDIR /app

# Copy built jar
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]