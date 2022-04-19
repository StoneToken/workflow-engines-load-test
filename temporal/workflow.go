package app

import (
	"time"

	"go.temporal.io/sdk/workflow"
)

func Single(ctx workflow.Context, name string) (string, error) {
	options := workflow.ActivityOptions{
		StartToCloseTimeout: time.Second * 5,
	}
	ctx = workflow.WithActivityOptions(ctx, options)
	var user string
	err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &user)
	return user, err
}

func Sequential(ctx workflow.Context, name string, count int) ([]string, error) {
	options := workflow.ActivityOptions{
		StartToCloseTimeout: time.Second * 5,
	}

	ctx = workflow.WithActivityOptions(ctx, options)

	userArray := make([]string, count)

	for i := 0; i < count; i++ {
		var user string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &user)
		userArray[i] = user
		if err != nil {
			return userArray, err
		}
	}

	return userArray, nil
}
