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
import ru.sbt.integration.orchestration.fsmcore.fluent.messagingapi.call.CallBuilderRetryPolicy;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;
import ru.sbt.jbpel.api.data.HttpStatusKt;
import ru.sbt.jbpel.api.data.SuccessMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ParallelFSM extends FSM {

  @Override
  @NotNull
  @Transition(Event = "CallTimeout", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallError", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "CallSuccess", MethodName = "errorAndTimeoutHandler")
  @Transition(Event = "ResponseReceived", MethodName = "responseReceivedHandler")
  public Action start(@NotNull Event event) {
    getContext().getLogger().info("Start ParallelFSM");
    EventStartTransaction start = (EventStartTransaction) event;
    JsonRpcRequest request = (JsonRpcRequest) start.getRequest();
    getContext().getStateData().put("users", new ArrayList<>(7));
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

  @Transition(Event = "CallSuccess", MethodName = "end")
  @Transition(Event = "ResponseReceived", MethodName = "responseReceivedHandler")
  public Action responseReceivedHandler(Event event) {
    int currentCount = getAndIncrement();
    getContext().getLogger().info("Entered responseReceivedHandler");
    @SuppressWarnings("unchecked")
    ArrayList<User> users = (ArrayList<User>) getContext().getStateData().get("users");
    if (event instanceof EventResponseReceived) {
      EventResponseReceived eventResponseReceived = (EventResponseReceived) event;
      RestReply restReply = (RestReply) eventResponseReceived.getReply();
      users.add(restReply.getReplyValue());
    }
    if (currentCount > 6) {
      getContext().getLogger().info("All rest calls responses were received");
      return action()
        .reply()
        .message(new JsonRpcReply(HttpStatusKt.OK, users.toArray(new User[0])))
        .build();
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
