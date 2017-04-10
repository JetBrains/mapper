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

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;

public class CompositesWithBounds {
  private final int myThreshold;

  public CompositesWithBounds(int threshold) {
    myThreshold = threshold;
  }

  public boolean isAbove(Rectangle upperBounds, Rectangle lowerBounds) {
    return upperBounds.origin.y + upperBounds.dimension.y - myThreshold <= lowerBounds.origin.y;
  }

  public boolean isBelow(Rectangle lower, Rectangle upper) {
    return isAbove(upper, lower);
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT homeElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT prev = Composites.prevFocusable(current);
      if (prev == null || isBelow(cell.getBounds(), prev.getBounds())) return current;
      current = prev;
    }
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT endElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT next = Composites.nextFocusable(current);
      if (next == null || isBelow(next.getBounds(), cell.getBounds())) return current;
      current = next;
    }
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasFocusableBounds>
  ViewT upperFocusable(ViewT v, int xOffset) {
    ViewT current = Composites.prevFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isAbove(current.getFocusableBounds(), bestMatch.getFocusableBounds())) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch.getFocusableBounds(), xOffset) > distanceTo(current.getFocusableBounds(), xOffset)) {
          bestMatch = current;
        }
      } else if (isAbove(current.getFocusableBounds(), v.getFocusableBounds())) {
        bestMatch = current;
      }

      current = Composites.prevFocusable(current);
    }

    return bestMatch;
  }

  public <ViewT extends NavComposite<ViewT> & HasFocusability & HasVisibility & HasFocusableBounds>
  ViewT lowerFocusable(ViewT v, int xOffset) {
    ViewT current = Composites.nextFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && isBelow(current.getFocusableBounds(), bestMatch.getFocusableBounds())) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch.getFocusableBounds(), xOffset) > distanceTo(current.getFocusableBounds(), xOffset)) {
          bestMatch = current;
        }
      } else if (isBelow(current.getFocusableBounds(), v.getFocusableBounds())) {
        bestMatch = current;
      }

      current = Composites.nextFocusable(current);
    }

    return bestMatch;
  }

  public double distanceTo(Rectangle bounds, int x) {
    return bounds.distance(new Vector(x, bounds.origin.y));
  }
}