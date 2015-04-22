package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Selector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SelectFromListEventSourcesTest {
  private SimpleEventSource<Object> es1 = new SimpleEventSource<>();
  private SimpleEventSource<Object> es2 = new SimpleEventSource<>();
  private SimpleEventSource<Object> es3 = new SimpleEventSource<>();
  private ObservableList<EventSource<Object>> list = new ObservableArrayList<>();
  private EventSource<Object> union = EventSources.selectList(list, new Selector<EventSource<Object>, EventSource<?>>() {
    @Override
    public EventSource<?> select(EventSource<Object> source) {
      return source;
    }
  });
  private EventHandler<Object> handler = Mockito.mock(EventHandler.class);
  private Registration reg;


  @Before
  public void before() {
    list.add(es1);
    list.add(es2);

    reg = union.addHandler(handler);
  }

  @Test
  public void fireOneEvent() {
    es1.fire("a");

    assertFired("a");
  }

  @Test
  public void addNewItem() {
    es3.fire("x");

    list.add(es3);
    es3.fire("z");

    assertFired("z");
  }

  @Test
  public void removeItem() {
    list.remove(es2);

    es2.fire("a");

    assertFired();
  }

  @Test
  public void removeReg() {
    reg.dispose();

    es1.fire("aaa");

    assertFired();
  }

  private void assertFired(Object... items) {
    for (Object s : items) {
      Mockito.verify(handler).onEvent(s);
    }
    Mockito.verifyNoMoreInteractions(handler);
  }




}
