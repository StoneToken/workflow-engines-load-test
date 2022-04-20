package app

import (
	"time"

	"go.temporal.io/sdk/workflow"
)

// Workflow executes a single REST call
func Single(ctx workflow.Context, name string) (string, error) {
	options := workflow.ActivityOptions{
		StartToCloseTimeout: time.Second * 5,
	}
	ctx = workflow.WithActivityOptions(ctx, options)
	var user string
	err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &user)
	return user, err
}

// Workflow executes N REST calls in a sequence
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

// Workflow executes 7 REST calls in parallel
func Parallel(ctx workflow.Context, name string) ([]string, error) {
	logger := workflow.GetLogger(ctx)
	defer logger.Info("Workflow completed.")

	ao := workflow.ActivityOptions{
		StartToCloseTimeout: 10 * time.Second,
	}
	ctx = workflow.WithActivityOptions(ctx, ao)

	// #1
	future1, settable1 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("First goroutine completed.")

		var results []string
		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		if err != nil {
			settable1.SetError(err)
			return
		}
		results = append(results, result)
		settable1.SetValue(results)
	})

	// #2
	future2, settable2 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("2 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable2.Set(result, err)
	})

	// #3
	future3, settable3 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("3 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable3.Set(result, err)
	})

	// #4
	future4, settable4 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("4 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable4.Set(result, err)
	})

	// #5
	future5, settable5 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("5 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable5.Set(result, err)
	})

	// #6
	future6, settable6 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("6 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable6.Set(result, err)
	})

	// #7
	future7, settable7 := workflow.NewFuture(ctx)
	workflow.Go(ctx, func(ctx workflow.Context) {
		defer logger.Info("7 goroutine completed.")

		var result string
		err := workflow.ExecuteActivity(ctx, RestTask, name).Get(ctx, &result)
		settable7.Set(result, err)
	})

	var results []string
	// Future.Get returns error from Settable.SetError
	// Note that the first goroutine puts a slice into the settable while the second a string value
	err := future1.Get(ctx, &results)
	if err != nil {
		return nil, err
	}
	var result string
	err = future2.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)
	err = future3.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)
	err = future4.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)
	err = future5.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)
	err = future6.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)
	err = future7.Get(ctx, &result)
	if err != nil {
		return nil, err
	}
	results = append(results, result)

	return results, nil
}
