package main

import (
	"log"
	"os"

	"go.temporal.io/sdk/client"
	"go.temporal.io/sdk/worker"

	"temporal/app"
)

func main() {
	// Create the client object just once per process
	// Default temporal server URL localhost:7233
	c, err := client.NewClient(client.Options{HostPort: os.Getenv("TEMPORAL_SERVER_URL")})
	if err != nil {
		log.Fatalln("unable to create Temporal client", err)
	}
	defer c.Close()
	// This worker hosts both Workflow and Activity functions
	w := worker.New(c, app.RestTaskQueue, worker.Options{})
	w.RegisterWorkflow(app.Single)
	w.RegisterWorkflow(app.Sequential)
	w.RegisterWorkflow(app.Parallel)
	w.RegisterActivity(app.RestTask)
	// Start listening to the Task Queue
	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("unable to start Worker", err)
	}
}
