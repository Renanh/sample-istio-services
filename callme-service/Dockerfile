# CRaC-enabled Dockerfile for callme-service
FROM azul/zulu-openjdk:25-jdk-crac-latest AS builder

WORKDIR /app

# Copy built jar from Maven
COPY target/*.jar app.jar

# Extract layers for efficient caching
RUN java -Djarmode=layertools -jar app.jar extract

FROM azul/zulu-openjdk:25-jdk-crac-latest

WORKDIR /app

# Copy extracted layers
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Create checkpoint directory
RUN mkdir -p /crac-files

# JVM flags for CRaC support
ENV JAVA_OPTS="-XX:CRaCCheckpointTo=/crac-files"

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
