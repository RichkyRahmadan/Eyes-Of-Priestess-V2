FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all POM files first to cache dependency resolution layer
COPY pom.xml ./pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY wallet-service/pom.xml ./wallet-service/pom.xml
COPY room-service/pom.xml ./room-service/pom.xml
COPY chat-service/pom.xml ./chat-service/pom.xml
COPY dispute-service/pom.xml ./dispute-service/pom.xml

# Download all dependencies & plugins offline (cached as Docker layer)
RUN mvn dependency:go-offline -pl auth-service -am -B --no-transfer-progress 2>/dev/null || true

# Copy source code
COPY auth-service/src ./auth-service/src
COPY wallet-service/src ./wallet-service/src
COPY room-service/src ./room-service/src
COPY chat-service/src ./chat-service/src
COPY dispute-service/src ./dispute-service/src

# Build
RUN mvn package -DskipTests -pl auth-service -am -B --no-transfer-progress

FROM registry.access.redhat.com/ubi8/openjdk-21:1.18
ENV LANGUAGE='en_US:en'
COPY --from=build --chown=185 /app/auth-service/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/auth-service/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/auth-service/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/auth-service/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
