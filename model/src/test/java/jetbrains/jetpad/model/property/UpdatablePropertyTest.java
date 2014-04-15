package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class UpdatablePropertyTest {
  private String value;
  private UpdatableProperty<String> property;

  @Before
  public void init() {
    property = new UpdatableProperty<String>() {
      @Override
      protected String doGet() {
        return value;
      }
    };
  }

  @Test
  public void simpleGet() {
    value = "z";

    assertEquals("z", property.get());
  }

  @Test
  public void getWithListenersDoesntGetWithoutUpdate() {
    value = "a";
    property.addHandler(Mockito.mock(EventHandler.class));
    value = "b";

    assertEquals("a", property.get());
  }

  @Test
  public void updateFiresEvent() {
    EventHandler<? super PropertyChangeEvent<String>> handler = Mockito.mock(EventHandler.class);

    property.addHandler(handler);
    value = "z";

    property.update();

    Mockito.verify(handler).onEvent(new PropertyChangeEvent<>(null, "z"));
  }

  @Test
  public void removeAllListenersReturnsToSimpleMode() {
    property.addHandler(Mockito.mock(EventHandler.class)).remove();

    value = "c";

    assertEquals("c", property.get());
  }

}
