package jetbrains.jetpad.geometry;

public enum Direction {
  Horizontal {
    @Override
    public <ValueT> ValueT coord(TwoDimensional<ValueT> td) {
      return td.getX();
    }
  },
  Vertical {
    @Override
    public <ValueT> ValueT coord(TwoDimensional<ValueT> td) {
      return td.getY();
    }
  };

  public abstract <ValueT> ValueT coord(TwoDimensional<ValueT> td);

  public Direction other() {
    if (this == Direction.Horizontal) {
      return Direction.Vertical;
    } else {
      return Direction.Horizontal;
    }
  }
}
