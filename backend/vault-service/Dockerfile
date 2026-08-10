FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY seal-service seal-service
COPY vault-service vault-service
COPY covenant-service covenant-service
COPY communion-service communion-service
COPY judgment-service judgment-service
COPY the-veil the-veil
RUN mvn package -DskipTests -pl vault-service -am

FROM registry.access.redhat.com/ubi8/openjdk-21:1.18
ENV LANGUAGE='en_US:en'
COPY --from=build --chown=185 /app/vault-service/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/vault-service/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/vault-service/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/vault-service/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8082
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
