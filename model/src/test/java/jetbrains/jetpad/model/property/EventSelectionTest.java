package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.event.SimpleEventSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventSelectionTest {
  SimpleEventSource<Object> es1 = new SimpleEventSource<>();
  SimpleEventSource<Object> es2 = new SimpleEventSource<>();
  Property<Boolean> prop = new ValueProperty<>(false);
  EventSource<Object> result = Properties.selectEvent(prop, new Selector<Boolean, EventSource<Object>>() {
    @Override
    public EventSource<Object> select(Boolean source) {
      return source ? es1 : es2;
    }
  });
  EventHandler<Object> handler = Mockito.mock(EventHandler.class);
  Registration reg;

  @Before
  public void before() {
    reg = result.addHandler(handler);
  }


  @Test
  public void ignoredEvent() {
    es1.fire(null);

    Mockito.verifyNoMoreInteractions(handler);
  }


  @Test
  public void event() {
    es2.fire(null);

    Mockito.verify(handler).onEvent(null);
    Mockito.verifyNoMoreInteractions(handler);
  }

  @Test
  public void switchEvents() {
    es1.fire("a");
    es2.fire("b");

    prop.set(true);
    es2.fire("c");
    es1.fire("d");

    Mockito.verify(handler).onEvent("b");
    Mockito.verify(handler).onEvent("d");
    Mockito.verifyNoMoreInteractions(handler);
  }

  @Test
  public void unregister() {
    reg.remove();

    es2.fire("c");

    Mockito.verifyNoMoreInteractions(handler);
  }
}
