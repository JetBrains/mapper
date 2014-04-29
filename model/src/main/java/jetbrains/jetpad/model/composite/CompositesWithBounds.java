package jetbrains.jetpad.model.composite;

import com.google.common.collect.Range;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

public class CompositesWithBounds {
  private int myThreshold;

  public CompositesWithBounds(int threshold) {
    myThreshold = threshold;
  }

  public <ViewT extends HasBounds> boolean isAbove(ViewT upper, ViewT lower) {
    Rectangle upperBounds = upper.getBounds();
    Rectangle lowerBounds = lower.getBounds();
    return upperBounds.origin.y + upperBounds.dimension.y - myThreshold <= lowerBounds.origin.y;
  }

  public <ViewT extends HasBounds> boolean isBelow(ViewT lower, ViewT upper) {
    return isAbove(upper, lower);
  }

  public <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT homeElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT prev = Composites.prevFocusable(current);
      if (prev == null || isAbove(prev, cell)) return current;
      current = prev;
    }
  }

  public <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT endElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT next = Composites.nextFocusable(current);
      if (next == null || isBelow(next, cell)) return current;
      current = next;
    }
  }

  public <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT upperFocusable(ViewT v, int xOffset) {
    ViewT current = Composites.prevFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isAbove(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (isAbove(current, v)) {
        bestMatch = current;
      }

      current = Composites.prevFocusable(current);
    }

    return bestMatch;
  }

  public <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT lowerFocusable(ViewT v, int xOffset) {
    ViewT current = Composites.nextFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isBelow(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (isBelow(current, v)) {
        bestMatch = current;
      }

      current = Composites.nextFocusable(current);
    }

    return bestMatch;
  }

  public <ViewT extends HasBounds> double distanceTo(ViewT c, int x) {
    Rectangle bounds = c.getBounds();
    return bounds.distance(new Vector(x, bounds.origin.y));
  }

  public <ViewT extends Composite<ViewT> & HasBounds & HasVisibility & HasFocusability>
  ViewT findClosest(ViewT current, Vector loc) {
    if (!current.visible().get()) return null;

    Rectangle bounds = current.getBounds();
    Range<Integer> range = Range.closed(bounds.origin.y, bounds.origin.y + bounds.dimension.y);
    if (!range.contains(loc.y)) {
      return null;
    }
    ViewT result = null;
    int distance = Integer.MAX_VALUE;
    for (ViewT child : current.children()) {
      if (!child.visible().get()) continue;

      ViewT closest = findClosest(child, loc);
      if (closest == null) continue;
      int newDistance = (int) closest.getBounds().distance(loc);

      if (newDistance < distance) {
        result = closest;
        distance = newDistance;
      }
    }

    if (result == null && current.focusable().get()) {
      return current;
    }

    return result;
  }
}
