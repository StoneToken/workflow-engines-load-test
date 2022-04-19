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

# Start workflow with CLI
tctl workflow run --tq REST_TASK_QUEUE --wt Sequential --et 60 -i '"test"' -i 5

# view help messages for workflow run
tctl workflow run -h

# Run worker
go run worker/main.go

# Start workflow with client
go run start/main.go

```