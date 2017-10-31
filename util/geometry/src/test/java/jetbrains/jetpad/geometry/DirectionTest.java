package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DirectionTest {

  private final Vector v = new Vector(100, 200);
  private final DoubleVector dv = new DoubleVector(100.001, 200.002);

  @Test
  public void horizontal() {
    assertEquals(100, Direction.Horizontal.coord(v).intValue());
    assertEquals(new Double(100.001), Direction.Horizontal.coord(dv));
  }

  @Test
  public void vertical() {
    assertEquals(200, Direction.Vertical.coord(v).intValue());
    assertEquals(new Double(200.002), Direction.Vertical.coord(dv));
  }

  @Test
  public void other() {
    assertSame(Direction.Vertical, Direction.Horizontal.other());
    assertSame(Direction.Horizontal, Direction.Vertical.other());
  }
}
