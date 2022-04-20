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

public class SingleFSM extends FSM {

    @Override
    @NotNull
    @Transition(Event = "CallTimeout", MethodName = "end")
    @Transition(Event = "CallError", MethodName = "end")
    @Transition(Event = "CallSuccess", MethodName = "end")
    @Transition(Event = "ResponseReceived", MethodName = "end")
    public Action start(@NotNull Event event) {
        getContext().getLogger().info("Start SingleFSM");
        EventStartTransaction start = (EventStartTransaction) event;
        JsonRpcRequest request = (JsonRpcRequest) start.getRequest();

        return action()
                .call()
                .remoteServiceName("remoteRestService")
                .message(new RestRequest(Collections.emptyMap(), request.getUsername()))
                .waitReplyFor(10000, TimeUnit.MILLISECONDS)
                .build();
    }

    @End
    public Action end(Event event) {
        getContext().getLogger().info("END " + event.getName());
        return action().finishExecution();
    }
}
