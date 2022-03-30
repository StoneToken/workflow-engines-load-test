# syntax=docker/dockerfile:1

FROM golang:1.16-alpine

WORKDIR /app

COPY go.mod ./
COPY go.sum ./
RUN go mod download

COPY *.go ./

RUN go build -o /money-transfer-project-template-go-workflow

EXPOSE 8080

CMD [ "/money-transfer-project-template-go-workflow" ]