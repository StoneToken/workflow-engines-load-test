package app

import (
	"testing"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"go.temporal.io/sdk/testsuite"
)

func Test_Workflow(t *testing.T) {
	testSuite := &testsuite.WorkflowTestSuite{}
	env := testSuite.NewTestWorkflowEnvironment()
	// Mock activity implementation
	env.OnActivity(RestTask, mock.Anything).Return("Hello World!", nil)
	env.ExecuteWorkflow(Single, "Test")
	require.True(t, env.IsWorkflowCompleted())
	require.NoError(t, env.GetWorkflowError())
	var user string
	require.NoError(t, env.GetWorkflowResult(&user))
	require.Equal(t, "Hello World!", user)
}
