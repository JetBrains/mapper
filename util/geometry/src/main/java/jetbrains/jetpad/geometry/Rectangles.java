package jetbrains.jetpad.geometry;

/**
 * X axis positive direction is right;
 * Y axis positive direction is down.
 */
public class Rectangles {
  public static Rectangle zeroOrigin(Rectangle r) {
    return new Rectangle(Vector.ZERO, r.dimension);
  }

  public static int upperDistance(Rectangle inner, Rectangle outer) {
    assertOuterInner(outer, inner);
    return topLeft(inner).y - topLeft(outer).y;
  }

  public static int lowerDistance(Rectangle inner, Rectangle outer) {
    assertOuterInner(outer, inner);
    return bottomLeft(outer).y - bottomLeft(inner).y;
  }

  public static int leftDistance(Rectangle inner, Rectangle outer) {
    assertOuterInner(outer, inner);
    return topLeft(inner).x - topLeft(outer).x;
  }

  public static int rightDistance(Rectangle inner, Rectangle outer) {
    assertOuterInner(outer, inner);
    return topRight(outer).x - topRight(inner).x;
  }

  private static void assertOuterInner(Rectangle outer, Rectangle inner) {
    if (!outer.contains(inner)) {
      throw new IllegalArgumentException("Outer does not contain inner: outer = " + outer + ", inner = " + inner);
    }
  }

  public static Rectangle extendUp(Rectangle r, int distance) {
    Vector change = new Vector(0, distance);
    return new Rectangle(r.origin.sub(change), r.dimension.add(change));
  }

  public static Rectangle extendDown(Rectangle r, int distance) {
    return r.changeDimension(r.dimension.add(new Vector(0, distance)));
  }

  public static Rectangle extendLeft(Rectangle r, int distance) {
    Vector change = new Vector(distance, 0);
    return new Rectangle(r.origin.sub(change), r.dimension.add(change));
  }

  public static Rectangle extendRight(Rectangle r, int distance) {
    return r.changeDimension(r.dimension.add(new Vector(distance, 0)));
  }

  public static Rectangle extendSides(int left, Rectangle r, int right) {
    return extendRight(extendLeft(r, left), right);
  }

  public static Rectangle shrinkRight(Rectangle r, int distance) {
    if (r.dimension.x < distance) {
      throw new IllegalArgumentException("To small rectangle = " + r + ", distance = " + distance);
    }
    return r.changeDimension(r.dimension.sub(new Vector(distance, 0)));
  }

  private static Vector topLeft(Rectangle r) {
    return r.origin;
  }

  private static Vector topRight(Rectangle r) {
    return r.origin.add(new Vector(r.dimension.x, 0));
  }

  private static Vector bottomLeft(Rectangle r) {
    return r.origin.add(new Vector(0, r.dimension.y));
  }

  private Rectangles() {
  }
}
