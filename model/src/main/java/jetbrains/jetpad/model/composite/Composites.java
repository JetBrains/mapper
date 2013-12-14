/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Composites {
  public static <CompositeT extends Composite<CompositeT>>
  void removeFromParent(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return;
    parent.children().remove(c);
  }

  public static <CompositeT extends Composite<CompositeT>>
  boolean isNonCompositeChild(CompositeT c) {
    if (c.parent().get() == null) return false;
    return c.parent().get().children().indexOf(c) == -1;
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT nextSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index + 1 < parent.children().size()) {
      return parent.children().get(index + 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT prevSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index > 0) {
      return parent.children().get(index - 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT firstLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return firstLeaf(c.children().get(0));
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT lastLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return lastLeaf(c.children().get(c.children().size() - 1));
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT nextLeaf(CompositeT c) {
    CompositeT nextSibling = nextSibling(c);
    if (nextSibling != null) {
      return firstLeaf(nextSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return nextLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT prevLeaf(CompositeT c) {
    CompositeT prevSibling = prevSibling(c);
    if (prevSibling != null) {
      return lastLeaf(prevSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return prevLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>>
  Iterable<CompositeT> ancestors(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrent = current.parent().get();

          @Override
          public boolean hasNext() {
            return myCurrent != null;
          }

          @Override
          public CompositeT next() {
            CompositeT result = myCurrent;
            myCurrent = myCurrent.parent().get();
            return result;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <CompositeT extends Composite<CompositeT>>
  Iterable<CompositeT> nextLeaves(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrentLeaf = current;
          private CompositeT myNextLeaf = nextLeaf(current);

          @Override
          public boolean hasNext() {
            return myNextLeaf != null;
          }

          @Override
          public CompositeT next() {
            myCurrentLeaf = myNextLeaf;
            myNextLeaf = nextLeaf(myCurrentLeaf);
            return myCurrentLeaf;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <CompositeT extends Composite<CompositeT>>
  Iterable<CompositeT> prevLeaves(final CompositeT current) {
    return new Iterable<CompositeT>() {
      @Override
      public Iterator<CompositeT> iterator() {
        return new Iterator<CompositeT>() {
          private CompositeT myCurrentLeaf = current;
          private CompositeT myPrevLeaf = prevLeaf(current);

          @Override
          public boolean hasNext() {
            return myPrevLeaf != null;
          }

          @Override
          public CompositeT next() {
            myCurrentLeaf = myPrevLeaf;
            myPrevLeaf = prevLeaf(myCurrentLeaf);
            return myCurrentLeaf;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <CompositeT extends Composite<CompositeT>>
  boolean isBefore(CompositeT c1, CompositeT c2) {
    if (c1 == c2) return false;

    List<CompositeT> c1a = reverseAncestors(c1);
    List<CompositeT> c2a = reverseAncestors(c2);

    if (c1a.get(0) != c2a.get(0)) {
      throw new IllegalArgumentException("Items are in different trees");
    }

    int commonLength = Math.min(c1a.size(), c2a.size());
    for (int i = 1; i < commonLength; i++) {
      CompositeT prevAncestor = c1a.get(i - 1);
      if (c1a.get(i) != c2a.get(i)) {
        int c1aIndex = prevAncestor.children().indexOf(c1a.get(i));
        int c2aIndex = prevAncestor.children().indexOf(c2a.get(i));
        return c1aIndex < c2aIndex;
      }
    }

    throw new IllegalArgumentException("One parameter is an ancestor of the other");
  }

  public static <CompositeT extends Composite<CompositeT>>
  boolean isDescendant(CompositeT ancestor, CompositeT descendant) {
    if (ancestor == descendant) return true;
    if (descendant.parent().get() == null) return false;
    return isDescendant(ancestor, descendant.parent().get());
  }

  private static <CompositeT extends Composite<CompositeT>>
  List<CompositeT> reverseAncestors(CompositeT c) {
    List<CompositeT> result = toList(ancestors(c));
    Collections.reverse(result);
    result.add(c);
    return result;
  }

  static <ItemT> List<ItemT> toList(Iterable<ItemT> it) {
    List<ItemT> result = new ArrayList<ItemT>();
    for (ItemT i : it) {
      result.add(i);
    }
    return result;
  }

  public static <ViewT extends Composite<ViewT> & HasVisibility>
  boolean isLastChild(ViewT v) {
    ViewT parent = v.parent().get();
    if (parent == null) return false;
    List<ViewT> siblings = parent.children();
    int index = siblings.indexOf(v);
    for (ViewT cv : siblings.subList(index + 1, siblings.size())) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <ViewT extends Composite<ViewT> & HasVisibility>
  boolean isFirstChild(ViewT cell) {
    ViewT parent = cell.parent().get();
    if (parent == null) return false;
    List<ViewT> siblings = parent.children();
    int index = siblings.indexOf(cell);

    for (ViewT cv : siblings.subList(0, index)) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT firstFocusable(ViewT v) {
    return firstFocusable(v, true);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT firstFocusable(ViewT v, boolean deepest) {
    for (ViewT cv : v.children()) {
      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      ViewT result = firstFocusable(cv);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;

    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT lastFocusable(ViewT c) {
    return lastFocusable(c, true);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT lastFocusable(ViewT v, boolean deepest) {
    List<ViewT> children = v.children();
    for (int i = children.size() - 1; i >= 0; i--) {
      ViewT cv = children.get(i);

      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      ViewT result = lastFocusable(cv, deepest);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;
    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasVisibility>
  boolean isVisible(ViewT v) {
    if (!v.visible().get()) return false;
    ViewT parent = v.parent().get();
    if (parent == null) return true;
    return isVisible(parent);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability>
  ViewT focusableParent(ViewT v) {
    ViewT parent = v.parent().get();
    if (parent == null) return null;
    if (parent.focusable().get()) return parent;
    return focusableParent(parent);
  }


  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  boolean isFocusable(ViewT v) {
    if (!v.focusable().get()) return false;

    ViewT current = v;
    while (current != null) {
      if (!current.visible().get()) return false;
      current = current.parent().get();
    }

    return true;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT firstFocusableLeaf(ViewT v) {
    List<ViewT> children = v.children();
    if (children.isEmpty() && v.focusable().get()) {
      return v;
    } else {
      for (int i = 0; i < children.size(); i++) {
        ViewT cv = children.get(i);
        if (cv.visible().get()) {
          return firstFocusable(cv);
        }
      }
    }
    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT lastFocusableLeaf(ViewT v) {
    List<ViewT> children = v.children();
    if (children.isEmpty() && v.focusable().get()) {
      return v;
    } else {
      for (int i = children.size() - 1; i >= 0; i--) {
        ViewT cv = children.get(i);
        if (cv.visible().get()) {
          return firstFocusable(cv);
        }
      }
    }
    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT nextFocusable(ViewT v) {
    for (ViewT cv : Composites.nextLeaves(v)) {
      if (isFocusable(cv)) return cv;
    }
    return null;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility>
  ViewT prevFocusable(ViewT v) {
    for (ViewT cv : Composites.prevLeaves(v)) {
      if (isFocusable(cv)) return cv;
    }
    return null;
  }

  public static <ViewT extends HasBounds> boolean isAbove(ViewT upper, ViewT lower) {
    Rectangle upperBounds = upper.getBounds();
    Rectangle lowerBounds = lower.getBounds();
    return upperBounds.origin.y + upperBounds.dimension.y <= lowerBounds.origin.y;
  }

  public static <ViewT extends HasBounds> boolean isBelow(ViewT lower, ViewT upper) {
    return isAbove(upper, lower);
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT homeElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT prev = Composites.prevFocusable(current);
      if (prev == null || isAbove(prev, cell)) return current;
      current = prev;
    }
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT endElement(ViewT cell) {
    ViewT current = cell;
    while (true) {
      ViewT next = Composites.nextFocusable(current);
      if (next == null || isBelow(next, cell)) return current;
      current = next;
    }
  }


  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT upperFocusable(ViewT v, int xOffset) {
    ViewT current = prevFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && Composites.isAbove(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (Composites.isAbove(current, v)) {
        bestMatch = current;
      }

      current = prevFocusable(current);
    }

    return bestMatch;
  }

  public static <ViewT extends Composite<ViewT> & HasFocusability & HasVisibility & HasBounds>
  ViewT lowerFocusable(ViewT v, int xOffset) {
    ViewT current = nextFocusable(v);
    ViewT bestMatch = null;

    while (current != null) {
      if (bestMatch != null && Composites.isBelow(current, bestMatch)) {
        break;
      }

      if (bestMatch != null) {
        if (distanceTo(bestMatch, xOffset) > distanceTo(current, xOffset)) {
          bestMatch = current;
        }
      } else if (Composites.isBelow(current, v)) {
        bestMatch = current;
      }

      current = nextFocusable(current);
    }

    return bestMatch;
  }

  public static <ViewT extends HasBounds> double distanceTo(ViewT c, int x) {
    Rectangle bounds = c.getBounds();
    return bounds.distance(new Vector(x, bounds.origin.y));
  }
}