package jetbrains.jetpad.model.event;

public class EventSources {
  public static <EventT> EventSource<EventT> composite(EventSource<? extends EventT>... sources) {
    return new CompositeEventSource<>(sources);
  }
}
