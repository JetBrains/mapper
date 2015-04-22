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

    assertFired();
  }


  @Test
  public void event() {
    es2.fire(null);

    assertFired((Object) null);
  }

  @Test
  public void switchEvents() {
    es1.fire("a");
    es2.fire("b");

    prop.set(true);
    es2.fire("c");
    es1.fire("d");

    assertFired("b", "d");
  }

  @Test
  public void unregister() {
    reg.remove();

    assertFired();
  }

  private void assertFired(Object... items) {
    for (Object s : items) {
      Mockito.verify(handler).onEvent(s);
    }
    Mockito.verifyNoMoreInteractions(handler);
  }

}
