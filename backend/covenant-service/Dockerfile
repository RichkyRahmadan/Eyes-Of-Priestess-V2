FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./pom.xml
COPY seal-service/pom.xml ./seal-service/pom.xml
COPY vault-service/pom.xml ./vault-service/pom.xml
COPY covenant-service/pom.xml ./covenant-service/pom.xml
COPY communion-service/pom.xml ./communion-service/pom.xml
COPY judgment-service/pom.xml ./judgment-service/pom.xml
COPY the-veil/pom.xml ./the-veil/pom.xml

RUN mvn dependency:go-offline -pl covenant-service -am -B --no-transfer-progress 2>/dev/null || true

COPY seal-service/src ./seal-service/src
COPY vault-service/src ./vault-service/src
COPY covenant-service/src ./covenant-service/src
COPY communion-service/src ./communion-service/src
COPY judgment-service/src ./judgment-service/src
COPY the-veil/src ./the-veil/src

RUN mvn package -DskipTests -pl covenant-service -am -B --no-transfer-progress

FROM registry.access.redhat.com/ubi8/openjdk-21:1.18
ENV LANGUAGE='en_US:en'
COPY --from=build --chown=185 /app/covenant-service/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/covenant-service/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/covenant-service/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/covenant-service/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8083
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
