package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DoubleRectangleTest {
  @Test
  public void hashCodeWorks() {
    assertEquals(new DoubleRectangle(DoubleVector.ZERO, DoubleVector.ZERO).hashCode(),
        new DoubleRectangle(DoubleVector.ZERO, DoubleVector.ZERO).hashCode());
  }

}
