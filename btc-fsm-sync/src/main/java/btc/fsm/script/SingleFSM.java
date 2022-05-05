package btc.fsm.script;

import btc.fsm.dto.User;
import btc.fsm.type.service.JsonRpcError;
import btc.fsm.type.service.JsonRpcReply;
import btc.fsm.type.service.JsonRpcRequest;
import btc.fsm.type.stub.RestReply;
import btc.fsm.type.stub.RestRequest;
import org.jetbrains.annotations.NotNull;
import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.ann.End;
import ru.sbt.integration.orchestration.fsmcore.ann.Transition;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.event.EventResponseReceived;
import ru.sbt.integration.orchestration.fsmcore.event.EventStartTransaction;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;
import ru.sbt.jbpel.api.data.HttpStatusKt;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SingleFSM extends FSM {
  @Override
  @NotNull
  @Transition(Event = "CallTimeout", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallError", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallException", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "ResponseReceived", MethodName = "responseReceivedHandler")
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

  @Transition(Event = "CallSuccess", MethodName = "end")
  public Action responseReceivedHandler(Event event) {
    EventResponseReceived eventResponseReceived = (EventResponseReceived) event;
    RestReply restReply = (RestReply) eventResponseReceived.getReply();
    getContext().getLogger().info("Entered responseReceivedHandler");
    return action()
      .reply()
      .message(new JsonRpcReply(HttpStatusKt.OK, new User[]{restReply.getReplyValue()}))
      .build();
  }

  @Transition(Event = "CallSuccess", MethodName = "end")
  public Action errorAndTimeoutHandler(Event event) {
    getContext().getLogger().info("Entered errorAndTimeoutHandler");
    return action()
      .reply()
      .message(
        new JsonRpcError(
          HttpStatusKt.INTERNAL_SERVER_ERROR,
          new ru.sbt.jbpel.api.data.JsonRpcError<>(
            1337,
            event.getType().name())))
      .build();
  }

  @End
  public Action end(Event event) {
    getContext().getLogger().info("END " + event.getName());
    return action().finishExecution();
  }
}
