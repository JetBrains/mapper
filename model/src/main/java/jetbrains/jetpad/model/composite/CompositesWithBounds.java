/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

public class CompositesWithBounds {
  private final int myThreshold;

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

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT homeElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT prev = Composites.prevFocusable(current);
      if (prev == null || isAbove(prev, cell)) return current;
      current = prev;
    }
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT endElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT next = Composites.nextFocusable(current);
      if (next == null || isBelow(next, cell)) return current;
      current = next;
    }
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  Iterable<ViewT> upperFocusables(final ViewT v) {
    return Composites.iterate(v, new NextUpperFocusable<ViewT>(v));
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  Iterable<ViewT> lowerFocusables(final ViewT v) {
    return Composites.iterate(v, new NextLowerFocusable<ViewT>(v));
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
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

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
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

  private class NextUpperFocusable<ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
      implements Function<ViewT, ViewT> {

    private final ViewT myFirstFocusableAbove;
    private final ViewT myInitial;

    NextUpperFocusable(ViewT initial) {
      myFirstFocusableAbove = firstFocusableAbove(initial);
      myInitial = initial;
    }

    private ViewT firstFocusableAbove(ViewT initial) {
      ViewT current = Composites.prevFocusable(initial);
      while (current != null && !isAbove(current, initial)) {
        current = Composites.prevFocusable(current);
      }
      return current;
    }

    @Override
    public ViewT apply(ViewT value) {
      if (value == myInitial) {
        return myFirstFocusableAbove;
      }
      ViewT next = Composites.prevFocusable(value);
      return next != null && !isAbove(next, myFirstFocusableAbove) ? next : null;
    }
  }

  private class NextLowerFocusable<ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
      implements Function<ViewT, ViewT> {

    private final ViewT myFirstFocusableBelow;
    private final ViewT myInitial;

    NextLowerFocusable(ViewT initial) {
      myFirstFocusableBelow = firstFocusableBelow(initial);
      myInitial = initial;
    }

    private ViewT firstFocusableBelow(ViewT initial) {
      ViewT current = Composites.nextFocusable(initial);
      while (current != null && !isBelow(current, initial)) {
        current = Composites.nextFocusable(current);
      }
      return current;
    }

    @Override
    public ViewT apply(ViewT value) {
      if (value == myInitial) {
        return myFirstFocusableBelow;
      }
      ViewT next = Composites.nextFocusable(value);
      return next != null && !isBelow(next, myFirstFocusableBelow) ? next : null;
    }
  }
}