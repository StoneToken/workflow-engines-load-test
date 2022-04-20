package btc.fsm.script;

import btc.fsm.type.service.JsonRpcTestRequest;
import btc.fsm.type.stub.RestRequest;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.ann.End;
import ru.sbt.integration.orchestration.fsmcore.ann.Transition;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.event.EventStartTransaction;
import ru.sbt.integration.orchestration.fsmcore.fluent.messagingapi.call.CallBuilderRetryPolicy;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ParallelFSM extends FSM {

    @Override
    @NotNull
    @Transition(Event = "CallTimeout", MethodName = "end")
    @Transition(Event = "CallError", MethodName = "end")
    @Transition(Event = "CallSuccess", MethodName = "end")
    @Transition(Event = "ResponseReceived", MethodName = "end")
    public Action start(@NotNull Event event) {
        EventStartTransaction start = (EventStartTransaction) event;
        JsonRpcTestRequest request = (JsonRpcTestRequest) start.getRequest();

        return action()
                .parallelCall()
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .add(createRemoteRestService(request))
                .build();
    }

    @NotNull
    private CallBuilderRetryPolicy createRemoteRestService(JsonRpcTestRequest request) {
        return action()
                .call()
                .remoteServiceName("remoteRestService")
                .message(new RestRequest(Collections.emptyMap(), request.getUsername()))
                .waitReplyFor(10000, TimeUnit.MILLISECONDS);
    }

    @End
    public Action end(Event event) {
        return action().finishExecution();
    }
}
