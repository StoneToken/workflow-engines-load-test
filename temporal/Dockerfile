FROM golang:1.18.0-alpine3.15 as build-env
# All these steps will be cached
RUN mkdir /app
WORKDIR /app
COPY go.mod . 
# <- COPY go.mod and go.sum files to the workspace
COPY go.sum .

# Get dependancies - will also be cached if we won't change mod/sum
RUN go mod download
# COPY the source code as the last step
COPY . .

# Build the binary
WORKDIR /app/worker
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a

FROM scratch
# <- Second step to build minimal image
COPY --from=build-env /app/worker /app
ENTRYPOINT ["/app/worker"]