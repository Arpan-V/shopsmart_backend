FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src/ src/
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:26-jre
WORKDIR /app

RUN useradd -m arpan
USER arpan

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]