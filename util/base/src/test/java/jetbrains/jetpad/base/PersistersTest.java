package jetbrains.jetpad.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PersistersTest {
  @Test
  public void nullInt() {
    testNull(Persisters.intPersister(10));
  }

  @Test
  public void nullLong() {
    testNull(Persisters.longPersister());
  }

  @Test
  public void nullBoolean() {
    testNull(Persisters.booleanPersister(true));
  }

  @Test
  public void nullDouble() {
    testNull(Persisters.doublePersister(1.5));
  }

  @Test
  public void nullString() {
    Persister<String> persister = Persisters.stringPersister();
    assertNull(persister.deserialize(persister.serialize(null)));
  }

  private <T> void testNull(Persister<T> persister) {
    T defaultValue = persister.deserialize(null);
    assertEquals(defaultValue, persister.deserialize(persister.serialize(null)));
  }
}
