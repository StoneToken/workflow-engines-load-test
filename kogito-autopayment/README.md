# Kogito bpmn load test

## Description

This project contains two modules

- kogito-autopayment -> bpmn use case from related client, workflow controls periodic account billing.
- kogito-autopayment-stub -> stub microservice for payment events

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

### Compile and Run in Local Dev Mode

```sh
mvn clean compile quarkus:dev
```

NOTE: With dev mode of Quarkus you can take advantage of hot reload for business assets like processes, rules, decision tables and java code. No need to redeploy or restart your running application.

### Package and Run in JVM mode

```sh
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

or on windows

```sh
mvn clean package
java -jar target\quarkus-app\quarkus-run.jar
```

### Package and Run using Local Native Image

Note that this requires GRAALVM_HOME to point to a valid GraalVM installation

```sh
mvn clean package -Pnative
```

To run the generated native executable, generated in `target/`, execute

```sh
./target/kogitobpmn-runner
```

### Build docker image

```sh
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/kogitobpmn-jvm .
```

### OpenAPI (Swagger) documentation

When running in either Quarkus Development or Native mode, we also leverage the [Quarkus OpenAPI extension](https://quarkus.io/guides/openapi-swaggerui#use-swagger-ui-for-development) that exposes [Swagger UI](http://localhost:8080/q/swagger-ui/) that you can use to look at available REST endpoints and send test requests.

### Stubs usage

#### Payment message stub

Use kafka to produce messages. Example start message, should be produced to topic payment.

```json
{
 "specversion": "0.3",
 "id": "21627e26-31eb-43e7-8343-92a696fd96b1",
 "source": "",
 "type": "payment",
 "time": "2022-02-24T13:25:16+0000",
 "data": {
  "id": 0,
  "sum": 0,
  "account": "string",
  "description": "string",
  "date": "string",
  "prepareResult": 0,
  "paymentResult": 0
 }
}
```

Example success message, will be produced to processedpayment topic

```json
{
 "id": "7bd1eb92-7823-4bce-b4ba-84989e5f8bef",
 "source": "",
 "type": "processedpayment",
 "time": "2022-04-26T12:52:22.27544042+03:00",
 "data": {
  "id": 0,
  "sum": 0,
  "account": "string",
  "description": "string",
  "date": "string",
  "prepareResult": 0,
  "paymentResult": 0
 },
 "specversion": "1.0",
 "kogitoprocinstanceid": "fa593208-873a-4d9a-9698-5a9b31268e3f",
 "kogitoprocid": "PaymentMessageStub",
 "kogitousertaskist": "1"
}
```

#### Payment REST stub

Start curl

```sh
curl -X 'POST' \
  'http://localhost:8080/PaymentRestStub' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{"payment":
{
    "id": 0,
    "sum": 0,
    "account": "string",
    "description": "string",
    "date": "string",
    "prepareResult": 0,
    "paymentResult": 0
  }
}'
```

Example response

```json
{
  "id": "a27c2d17-bef6-4ae4-9caa-06d7ac842162",
  "payment": {
    "id": 0,
    "sum": 0,
    "account": "string",
    "description": "string",
    "date": "string",
    "prepareResult": 0,
    "paymentResult": 0
  }
}
```

## Deploying with Kogito Operator

In the [`operator`](operator) directory you'll find the custom resources needed to deploy this example on OpenShift with the [Kogito Operator](https://docs.jboss.org/kogito/release/latest/html_single/#chap_kogito-deploying-on-openshift).
