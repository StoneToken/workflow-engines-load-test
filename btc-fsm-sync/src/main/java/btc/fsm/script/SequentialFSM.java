package btc.fsm.script;

import btc.fsm.type.service.JsonRpcRequest;
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
    @Transition(Event = "Custom.call", MethodName = "call")
    public Action start(@NotNull Event event) {
        getContext().getLogger().info("Start SequentialFSM");
        EventStartTransaction start = (EventStartTransaction) event;
        JsonRpcRequest request = (JsonRpcRequest) start.getRequest();
        getContext().getStateData().put("username", request.getUsername());
        return action().fireEvent("call");
    }

    @NotNull
    @Transition(Event = "CallTimeout", MethodName = "end")
    @Transition(Event = "CallError", MethodName = "end")
    @Transition(Event = "ResponseReceived", MethodName = "call")
    @Transition(Event = "Custom.end", MethodName = "end")
    public Action call(@NotNull Event event) {
        int counter = getAndIncrement();
        getContext().getLogger().info("Entered call");
        if (counter > 7) {
            getContext().getLogger().info("All rest calls were executed");
            return action().fireEvent("end");
        }
        getContext().getLogger().info("Rest call in " + counter + " time");
        return action()
                .call()
                .remoteServiceName("remoteRestService")
                .message(new RestRequest(Collections.emptyMap(), (String) getContext().getStateData().get("username")))
                .waitReplyFor(10000, TimeUnit.MILLISECONDS)
                .build();
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
