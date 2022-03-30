# Temporal load test cases

## Design

![Схема размещения](./docker-compose/docker-compose.png)

```sh
# Prepare environment
alias tctl="docker exec temporal-admin-tools tctl"

```если вы уже проходили эту ошибку, то с

## Runtime

```sh
docker-compose up -d

# Create namespace
tctl --ns test-namespace namespace register -rd 1

# Run worker
go run worker/main.go

# Run workflow
go run start/main.go

``` 