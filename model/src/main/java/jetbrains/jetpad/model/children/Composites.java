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
package jetbrains.jetpad.model.children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Composites {
  public static <CompositeT extends Composite<CompositeT>> void removeFromParent(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return;
    parent.children().remove(c);
  }

  public static <CompositeT extends Composite<CompositeT>> boolean isNonCompositeChild(CompositeT c) {
    if (c.parent().get() == null) return false;
    return c.parent().get().children().indexOf(c) == -1;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT nextSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index + 1 < parent.children().size()) {
      return parent.children().get(index + 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT prevSibling(CompositeT c) {
    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    if (isNonCompositeChild(c)) return null;
    int index = parent.children().indexOf(c);
    if (index > 0) {
      return parent.children().get(index - 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT firstLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return firstLeaf(c.children().get(0));
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT lastLeaf(CompositeT c) {
    if (c.children().isEmpty()) return c;
    return lastLeaf(c.children().get(c.children().size() - 1));
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT nextLeaf(CompositeT c) {
    CompositeT nextSibling = nextSibling(c);
    if (nextSibling != null) {
      return firstLeaf(nextSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return nextLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>> CompositeT prevLeaf(CompositeT c) {
    CompositeT prevSibling = prevSibling(c);
    if (prevSibling != null) {
      return lastLeaf(prevSibling);
    }

    if (isNonCompositeChild(c)) return null;

    CompositeT parent = c.parent().get();
    if (parent == null) return null;
    return prevLeaf(parent);
  }

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> ancestors(final CompositeT current) {
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

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> nextLeaves(final CompositeT current) {
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

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> prevLeaves(final CompositeT current) {
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

  public static <CompositeT extends Composite<CompositeT>> boolean isBefore(CompositeT c1, CompositeT c2) {
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

  private static <CompositeT extends Composite<CompositeT>> List<CompositeT> reverseAncestors(CompositeT c) {
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
}