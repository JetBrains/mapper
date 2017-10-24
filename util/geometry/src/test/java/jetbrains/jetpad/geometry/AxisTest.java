package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AxisTest {

  @Test
  public void getOtherAxis() {
    assertEquals(Axis.Y, Axis.X.getOtherAxis());
    assertEquals(Axis.X, Axis.Y.getOtherAxis());
  }

}
