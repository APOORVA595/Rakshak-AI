# ---------- Stage 1: Build the app ----------
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy only the files needed to download dependencies first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy the actual source code and build the jar
COPY src src
RUN ./mvnw clean package -DskipTests -B

# ---------- Stage 2: Run the app ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
