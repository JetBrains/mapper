package jetbrains.jetpad.base;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntervalTest {
  private static Interval i(int lower, int upper) {
    return new Interval(lower, upper);
  }

  @Test
  public void contains() {
    assertTrue(i(1, 4).contains(i(1, 4)));
    assertTrue(i(1, 4).contains(i(2, 3)));
    assertTrue(i(1, 4).contains(i(1, 2)));
    assertTrue(i(1, 4).contains(i(3, 4)));

    assertFalse(i(1, 4).contains(i(0, 4)));
    assertFalse(i(1, 4).contains(i(1, 5)));
    assertFalse(i(1, 4).contains(i(0, 5)));
    assertFalse(i(1, 4).contains(i(4, 10)));
  }

  @Test
  public void intersects() {
    assertTrue(i(1, 4).intersects(i(1, 4)));
    assertTrue(i(1, 4).intersects(i(2, 3)));
    assertTrue(i(1, 4).intersects(i(1, 2)));
    assertTrue(i(1, 4).intersects(i(3, 4)));

    assertTrue(i(1, 4).intersects(i(0, 4)));
    assertTrue(i(1, 4).intersects(i(0, 1)));
    assertTrue(i(1, 4).intersects(i(2, 5)));
    assertTrue(i(1, 4).intersects(i(4, 5)));

    assertTrue(i(1, 4).intersects(i(0, 5)));

    assertFalse(i(1, 4).intersects(i(-1, 0)));
    assertFalse(i(1, 4).intersects(i(5, 6)));
  }
}
