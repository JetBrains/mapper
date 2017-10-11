package jetbrains.jetpad.geometry;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DoubleRectangleEdgesTest {

  private static final double LEFT = 5.;
  private static final double RIGHT = 15.;

  private static final double TOP = 40.;
  private static final double BOTTOM = 60.;

  private static final DoubleVector LEFT_TOP = new DoubleVector(LEFT, TOP);
  private static final DoubleVector LEFT_BOTTOM = new DoubleVector(LEFT, BOTTOM);
  private static final DoubleVector RIGHT_TOP = new DoubleVector(RIGHT, TOP);
  private static final DoubleVector RIGHT_BOTTOM = new DoubleVector(RIGHT, BOTTOM);

  private static final DoubleSegment LEFT_EDGE = edge(LEFT_TOP, LEFT_BOTTOM);
  private static final DoubleSegment BOTTOM_EDGE = edge(RIGHT_BOTTOM, LEFT_BOTTOM);
  private static final DoubleSegment RIGHT_EDGE = edge(RIGHT_BOTTOM, RIGHT_TOP);
  private static final DoubleSegment TOP_EDGE = edge(LEFT_TOP, RIGHT_TOP);

  private static final DoubleRectangle RECTANGLE = new DoubleRectangle(LEFT_TOP, RIGHT_BOTTOM.subtract(LEFT_TOP));

  @Test
  public void getLeftEdge() {
    assertEquals(LEFT_EDGE, RECTANGLE.getLeftEdge());
  }

  @Test
  public void getTopEdge() {
    assertEquals(TOP_EDGE, RECTANGLE.getTopEdge());
  }

  @Test
  public void getRightEdge() {
    assertEquals(RIGHT_EDGE, RECTANGLE.getRightEdge());
  }

  @Test
  public void getBottomEdge() {
    assertEquals(BOTTOM_EDGE, RECTANGLE.getBottomEdge());
  }

  @Test
  public void getParts() {
    assertEquals(
        asList(TOP_EDGE, LEFT_EDGE, RIGHT_EDGE, BOTTOM_EDGE),
        RECTANGLE.getParts()
    );
  }

  private static DoubleSegment edge(DoubleVector start, DoubleVector end) {
    return new DoubleSegment(start, end);
  }

}