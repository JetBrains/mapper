package jetbrains.jetpad.model.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Event Handler which logs all events for test purposes
 */
public class LoggingEventHandler<EventT> implements EventHandler<EventT> {
  private List<EventT> myEvents = new ArrayList<>();

  @Override
  public void onEvent(EventT event) {
    myEvents.add(event);
  }

  public List<EventT> getEvents() {
    return Collections.unmodifiableList(myEvents);
  }
}
