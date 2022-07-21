# Kogito bpmn load test

## Description

Architecture

![docker-compose](docker-compose/docker-compose.png)

## Build and run

### Prerequisites

You will need:

- Java 11+ installed
- Environment variable JAVA_HOME set accordingly
- Maven 3.8.1+ installed

When using native image compilation, you will also need:

- GraalVM 19.1+ installed
- Environment variable GRAALVM_HOME set accordingly
- Note that GraalVM native image compilation typically requires other packages (glibc-devel, zlib-devel and gcc) to be installed too, please refer to GraalVM installation documentation for more details.

### Profiles

I have added a few profiles for kogito persistence: infinispan, postgresql and kafka, they should be activated as quarkus profiles like.

```sh
mvn clean package -Dquarkus-profile=postgresql
```
NOTE: Parallel test will fail in app built with *kafka* persistance profile, because [kafka persistence doesn't support optimistic locks](https://issues.redhat.com/browse/KOGITO-7229)

Also, there is a few profiles for kogito runtime and jobs service integration: rest-jobs and kafka-jobs. They should be activated as plain maven profile.

```sh
mvn clean package -P kafka-jobs
```

Profile specific config is prefixed with % in .props file.

### Compile and Run in Local Dev Mode

```sh
mvn clean compile quarkus:dev -Dquarkus-profile=%PERSISTANCE% -P %JOBS_INTEGRATION%
```

NOTE: With dev mode of Quarkus you can take advantage of hot reload for business assets like processes, rules, decision tables and java code. No need to redeploy or restart your running application.

### Package and Run in JVM mode

```sh
mvn clean package -Dquarkus-profile=%PERSISTANCE% -P %JOBS_INTEGRATION%
java -jar target/quarkus-app/quarkus-run.jar -Dquarkus-profile=%PERSISTANCE%
```

or on windows

```sh
mvn clean package -Dquarkus-profile=%PERSISTANCE% -P %JOBS_INTEGRATION%
java -jar target\quarkus-app\quarkus-run.jar -Dquarkus-profile=%PERSISTANCE% 
```

### Package and Run using Local Native Image

Note that this requires GRAALVM_HOME to point to a valid GraalVM installation

```sh
mvn clean package -Dquarkus-profile=%PERSISTANCE% -Pnative,%JOBS_INTEGRATION%
```

To run the generated native executable, generated in `target/`, execute

```sh
./target/kogitobpmn-runner
```

### Build docker image

```sh
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/kogito-rnd-jvm .
```

### OpenAPI (Swagger) documentation

[Specification at swagger.io](https://swagger.io/docs/specification/about/)

You can take a look at the [OpenAPI definition](http://localhost:8080/openapi?format=json) - automatically generated and included in this service - to determine all available operations exposed by this service. For easy readability you can visualize the OpenAPI definition file using a UI tool like for example available [Swagger UI](https://editor.swagger.io).

In addition, various clients to interact with this service can be easily generated using this OpenAPI definition.

When running in either Quarkus Development or Native mode, we also leverage the [Quarkus OpenAPI extension](https://quarkus.io/guides/openapi-swaggerui#use-swagger-ui-for-development) that exposes [Swagger UI](http://localhost:8080/swagger-ui/) that you can use to look at available REST endpoints and send test requests.


### Start load test

Start docker-compose

```sh
docker-compose -f docker-compose/docker-compose.yml up -d
```

If you're using **kafka** persistence profile, create topic *kogito.process* in kafka 

Curl for test

```sh
curl -X POST -H 'Content-Type:application/json' -H 'Accept:application/json' -d '{"username" : "test"}' http://localhost:8080/test
```

Prometheus metrics available at `host/q/metrics`

## Deploying with Kogito Operator

In the [`operator`](operator) directory you'll find the custom resources needed to deploy this example on OpenShift with the [Kogito Operator](https://docs.jboss.org/kogito/release/latest/html_single/#chap_kogito-deploying-on-openshift).
