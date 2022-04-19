package main

import (
	"context"
	"fmt"
	"log"

	"go.temporal.io/sdk/client"

	"temporal/app"
)

func main() {
	// Create the client object just once per process
	c, err := client.NewClient(client.Options{})
	if err != nil {
		log.Fatalln("unable to create Temporal client", err)
	}
	defer c.Close()
	options := client.StartWorkflowOptions{
		ID:        "Single",
		TaskQueue: app.RestTaskQueue,
	}
	name := "test"
	we, err := c.ExecuteWorkflow(context.Background(), options, app.Single, name)
	if err != nil {
		log.Fatalln("unable to complete Workflow", err)
	}
	var user string
	err = we.Get(context.Background(), &user)
	if err != nil {
		log.Fatalln("unable to get Workflow result", err)
	}
	printResults(user, we.GetID(), we.GetRunID())
}

func printResults(user string, workflowID, runID string) {
	fmt.Printf("\nWorkflowID: %s RunID: %s\n", workflowID, runID)
	fmt.Printf("\nUser data: \n%s\n", user)
}
