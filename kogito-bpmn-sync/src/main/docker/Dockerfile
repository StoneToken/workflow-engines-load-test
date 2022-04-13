####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the docker image run:
#
# mvn package
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile -t quarkus/kogitobpmn-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/kogitobpmn-jvm
#
###
FROM fabric8/java-alpine-openjdk11-jre
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV AB_ENABLED=jmx_exporter

EXPOSE 8080
COPY ../../../target /*.* /home/kogito/bin/

ENTRYPOINT ["java","-jar","/home/kogito/bin/quarkus-app/quarkus-run.jar"]