package btc.fsm.script;

import btc.fsm.dto.User;
import btc.fsm.type.service.JsonRpcReply;
import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.ann.End;
import ru.sbt.integration.orchestration.fsmcore.ann.Transition;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.HttpStatusKt;

import java.util.UUID;

public class StartStopFSM extends FSM {

  @Override
  @Transition(Event = "CallSuccess", MethodName = "end")
  public Action start(Event event) {
    getContext().getLogger().info("Start StartStopFSM");
    return action()
      .reply()
      .message(new JsonRpcReply(HttpStatusKt.OK, new User[0]))
      .build();
  }

  @End
  public Action end(Event event) {
    getContext().getLogger().info("END " + event.getName());
    return action().finishExecution();
  }
}
