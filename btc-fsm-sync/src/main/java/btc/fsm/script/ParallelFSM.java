package btc.fsm.script;

import btc.fsm.type.service.JsonRpcRequest;
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
    @Transition(Event = "ResponseReceived", MethodName = "responseReceivedHandler")
    public Action start(@NotNull Event event) {
        getContext().getLogger().info("Start ParallelFSM");
        EventStartTransaction start = (EventStartTransaction) event;
        JsonRpcRequest request = (JsonRpcRequest) start.getRequest();

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
    private CallBuilderRetryPolicy createRemoteRestService(JsonRpcRequest request) {
        return action()
                .call()
                .remoteServiceName("remoteRestService")
                .message(new RestRequest(Collections.emptyMap(), request.getUsername()))
                .waitReplyFor(10000, TimeUnit.MILLISECONDS);
    }

    @Transition(Event = "Custom.end", MethodName = "end")
    @Transition(Event = "ResponseReceived", MethodName = "responseReceivedHandler")
    public Action responseReceivedHandler(Event event) {
        int currentCount = getAndIncrement();
        getContext().getLogger().info("Entered responseReceivedHandler");

        if (currentCount > 6) {
            getContext().getLogger().info("All rest calls responses were received");
            return action().fireEvent("end");
        } else {
            getContext().getLogger().info(currentCount + " rest calls responses were received. Waiting for others…");
            return action().noOperation();
        }
    }

    private int getAndIncrement() {
        int oldValue = (int) getContext().getStateData().getOrDefault("counter", 1);
        getContext().getStateData().put("counter", oldValue + 1);
        return oldValue;
    }

    @End
    public Action end(Event event) {
        getContext().getLogger().info("END " + event.getName());
        return action().finishExecution();
    }
}
