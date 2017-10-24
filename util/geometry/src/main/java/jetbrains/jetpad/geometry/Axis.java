package jetbrains.jetpad.geometry;

public enum Axis {

  X, Y;

  public Axis getOtherAxis() {
    return AxisUtil.getOtherAxis(this);
  }

}
