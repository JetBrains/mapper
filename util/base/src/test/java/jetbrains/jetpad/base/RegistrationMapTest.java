package jetbrains.jetpad.base;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegistrationMapTest {

  private RegistrationMap<String> map;

  @Before
  public void init() {
    map = new RegistrationMap<>();
  }

  @Test
  public void dispose() {
    map.put("a", new Registration() {
      @Override
      protected void doRemove() {
        throw new RuntimeException("test");
      }
    });
    map.put("b", new Registration() {
      @Override
      protected void doRemove() {}
    });
    map.put("c", new Registration() {
      @Override
      protected void doRemove() {}
    });

    try {
      map.clear();
    } catch (RuntimeException e) {
      assertEquals("test", e.getMessage());
    }

    assertTrue(map.keys().isEmpty());
  }
}
