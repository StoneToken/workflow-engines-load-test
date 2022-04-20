package btc.fsm.script;

import btc.fsm.type.service.JsonRpcTestRequest;
import btc.fsm.type.stub.RestRequest;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.ann.End;
import ru.sbt.integration.orchestration.fsmcore.ann.Transition;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.event.EventStartTransaction;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SequentialFSM extends FSM {

    @Override
    @NotNull
    @Transition(Event = "Custom.event", MethodName = "call")
    public Action start(@NotNull Event event) {
        EventStartTransaction start = (EventStartTransaction) event;
        JsonRpcTestRequest request = (JsonRpcTestRequest) start.getRequest();
        getContext().getStateData().put("username", request.getUsername());
        return action().fireEvent("call");
    }

    @NotNull
    @Transition(Event = "CallTimeout", MethodName = "end")
    @Transition(Event = "CallError", MethodName = "end")
    @Transition(Event = "ResponseReceived", MethodName = "call")
    @Transition(Event = "Custom.event", MethodName = "end")
    public Action call(@NotNull Event event) {
        int counter = getAndIncrement();
        if (counter > 6) {
            return action().fireEvent("end");
        }
        return action()
                .call()
                .remoteServiceName("remoteRestService")
                .message(new RestRequest(Collections.emptyMap(), (String) getContext().getStateData().get("username")))
                .waitReplyFor(10000, TimeUnit.MILLISECONDS)
                .build();
    }

    private int getAndIncrement() {
        int oldValue = (int) getContext().getStateData().getOrDefault("counter", 0);
        getContext().getStateData().put("counter", oldValue + 1);
        return oldValue;
    }

    @End
    public Action end(Event event) {
        return action().finishExecution();
    }
}
