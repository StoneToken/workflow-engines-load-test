package btc.fsm.script;

import ru.sbt.integration.orchestration.fsmcore.FSM;
import ru.sbt.integration.orchestration.fsmcore.ann.End;
import ru.sbt.integration.orchestration.fsmcore.ann.Transition;
import ru.sbt.integration.orchestration.fsmcore.event.Event;
import ru.sbt.integration.orchestration.fsmcore.messages.Action;

public class StartStopFSM extends FSM {

    @Override
    @Transition(Event = "Custom.end", MethodName = "end")
    public Action start(Event event) {
        getContext().getLogger().info("Start StartStopFSM");
        return action().fireEvent("end");
    }

    @End
    public Action end(Event event) {
        getContext().getLogger().info("END " + event.getName());
        return action().finishExecution();
    }
}
