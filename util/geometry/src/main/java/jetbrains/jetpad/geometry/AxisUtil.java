package jetbrains.jetpad.geometry;

final class AxisUtil {

  static Axis getOtherAxis(Axis axis) {
    Axis result;
    switch (axis) {
      case X:
        result = Axis.Y;
        break;
      case Y:
        result = Axis.X;
        break;
      default:
        throw new UnsupportedOperationException("unknown axis: " + axis);
    }
    return result;
  }

  private AxisUtil() {
  }

}
