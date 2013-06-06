package jetbrains.jetpad.model.property;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BooleanPropertiesTest {
  private static Property<Boolean> TRUE = new ValueProperty<Boolean>(true);
  private static Property<Boolean> FALSE = new ValueProperty<Boolean>(false);
  private static Property<Boolean> NULL = new ValueProperty<Boolean>() {
    @Override
    public Boolean get() {
      return null;
    }
  };

  @Test
  public void not() {
    assertFalse(Properties.not(TRUE).get());
    assertTrue(Properties.not(FALSE).get());
    assertNull(Properties.not(NULL).get());
  }

  @Test
   public void and() {
    assertTrue(Properties.and(TRUE, TRUE).get());
    assertFalse(Properties.and(TRUE, FALSE).get());
    assertNull(Properties.and(TRUE, NULL).get());
    assertFalse(Properties.and(FALSE, TRUE).get());
    assertFalse(Properties.and(FALSE, FALSE).get());
    assertFalse(Properties.and(FALSE, NULL).get());
    assertNull(Properties.and(NULL, TRUE).get());
    assertFalse(Properties.and(NULL, FALSE).get());
    assertNull(Properties.and(NULL, NULL).get());
  }

  @Test
  public void or() {
    assertTrue(Properties.or(TRUE, TRUE).get());
    assertTrue(Properties.or(TRUE, FALSE).get());
    assertTrue(Properties.or(TRUE, NULL).get());
    assertTrue(Properties.or(FALSE, TRUE).get());
    assertFalse(Properties.or(FALSE, FALSE).get());
    assertNull(Properties.or(FALSE, NULL).get());
    assertTrue(Properties.or(NULL, TRUE).get());
    assertNull(Properties.or(NULL, FALSE).get());
    assertNull(Properties.or(NULL, NULL).get());
  }
}
