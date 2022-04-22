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

import java.util.ArrayList;
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
    getContext().getStateData().put("users", new ArrayList<>(7));
    return action().fireEvent("call");
  }

  @NotNull
  @Transition(Event = "CallTimeout", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallError", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallException", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallSuccess", MethodName = "end")
  @Transition(Event = "ResponseReceived", MethodName = "call")
  public Action call(@NotNull Event event) {
    getContext().getLogger().info("Entered call");
    int counter = getAndIncrement();
    if (event instanceof EventResponseReceived) {
      @SuppressWarnings("unchecked")
      ArrayList<User> users = (ArrayList<User>) getContext().getStateData().get("users");
      RestReply restReply = (RestReply) ((EventResponseReceived) event).getReply();
      users.add(restReply.getReplyValue());
      if (counter > 7) {
        getContext().getLogger().info("All rest calls were executed");
        return action()
          .reply()
          .message(new JsonRpcReply(HttpStatusKt.OK, users.toArray(new User[0])))
          .build();
      }
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
