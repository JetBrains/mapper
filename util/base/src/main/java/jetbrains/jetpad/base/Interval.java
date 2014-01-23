package jetbrains.jetpad.base;

public class Interval {
  private int myLowerBound;
  private int myUpperBound;

  public Interval(int lowerBound, int upperBound) {
    if (lowerBound > upperBound) throw new IllegalArgumentException("Lower bound is greater than upper: lower bound=" + lowerBound + ", upper bound=" + upperBound);
    myLowerBound = lowerBound;
    myUpperBound = upperBound;
  }

  public int getLowerBound() {
    return myLowerBound;
  }

  public int getUpperBound() {
    return myUpperBound;
  }

  @Override
  public String toString() {
    return "[" + myLowerBound + ", " + myUpperBound + "]";
  }
}
