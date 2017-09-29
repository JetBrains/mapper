/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import com.google.common.collect.TreeTraverser;
import jetbrains.jetpad.base.Functions;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

public final class Composites {
  private static CompositesWithBounds ourWithBounds = new CompositesWithBounds(0);

  private static final Predicate<?> IS_FOCUSABLE = new Predicate<HasFocusability>() {
    @Override
    public boolean test(HasFocusability value) {
      return value.focusable().get();
    }
  };

  private static final Predicate<?> IS_INVISIBLE = new Predicate<HasVisibility>() {
    @Override
    public boolean test(HasVisibility value) {
      return !value.visible().get();
    }
  };

  public static <HasParentT extends HasParent<HasParentT> & HasFocusability> Predicate<HasParentT> isFocusable() {
    @SuppressWarnings("unchecked")
    Predicate<HasParentT> predicate = (Predicate<HasParentT>) IS_FOCUSABLE;
    return predicate;
  }

  public static <HasParentT extends HasParent<HasParentT> & HasVisibility> Predicate<HasParentT> isInvisible() {
    @SuppressWarnings("unchecked")
    Predicate<HasParentT> predicate = (Predicate<HasParentT>) IS_INVISIBLE;
    return predicate;
  }

  public static <CompositeT extends Composite<CompositeT>>
  void removeFromParent(CompositeT c) {
    CompositeT parent = c.getParent();
    if (parent == null) return;
    parent.children().remove(c);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  boolean isNonCompositeChild(CompositeT c) {
    if (c.getParent() == null) return false;

    if (c.nextSibling() != null) return false;
    if (c.prevSibling() != null) return false;

    List<CompositeT> children = c.getParent().children();
    if (children.size() != 1) return true;
    if (children.get(0) != c) return true;

    return false;
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT nextSibling(CompositeT c) {
    CompositeT parent = c.getParent();
    if (parent == null) return null;
    int index = parent.children().indexOf(c);
    if (index == -1) return null;
    if (index + 1 < parent.children().size()) {
      return parent.children().get(index + 1);
    }
    return null;
  }

  public static <CompositeT extends Composite<CompositeT>>
  CompositeT prevSibling(CompositeT c) {
    CompositeT parent = c.getParent();
    if (parent == null) return null;
    int index = parent.children().indexOf(c);
    if (index == -1) return null;
    if (index > 0) {
      return parent.children().get(index - 1);
    }
    return null;
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT firstLeaf(CompositeT c) {
    CompositeT first = c.firstChild();
    if (first == null) return c;
    return firstLeaf(first);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT lastLeaf(CompositeT c) {
    CompositeT last = c.lastChild();
    if (last == null) return c;
    return lastLeaf(last);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT nextLeaf(CompositeT c) {
    return nextLeaf(c, null);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT nextLeaf(CompositeT c, CompositeT within) {
    CompositeT current = c;
    while (true) {
      CompositeT nextSibling = current.nextSibling();
      if (nextSibling != null) {
        return firstLeaf(nextSibling);
      }

      if (isNonCompositeChild(current)) return null;
      CompositeT parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT prevLeaf(CompositeT c) {
    return prevLeaf(c, null);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  CompositeT prevLeaf(CompositeT c, CompositeT within) {
    CompositeT current = c;
    while (true) {
      CompositeT prevSibling = current.prevSibling();
      if (prevSibling != null) {
        return lastLeaf(prevSibling);
      }

      if (isNonCompositeChild(current)) return null;

      CompositeT parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
  }

  public static <HasParentT extends HasParent<HasParentT>> HasParentT root(HasParentT current) {
    for (HasParentT c = current; ; c = c.getParent()) {
      if (c.getParent() == null) {
        return c;
      }
    }
  }

  /**
   * @return Iterable containing the current node and all ancestors.
   */
  public static <HasParentT extends HasParent<HasParentT>> Iterable<HasParentT> ancestorsFrom(HasParentT current) {
    return iterateFrom(current, new Function<HasParentT, HasParentT>() {
      @Override
      public HasParentT apply(HasParentT hasParent) {
        return hasParent.getParent();
      }
    });
  }

  /**
   * @return Iterable containing all ancestors, but not the current node.
   */
  public static <HasParentT extends HasParent<HasParentT>> Iterable<HasParentT> ancestors(HasParentT current) {
    return iterate(current, new Function<HasParentT, HasParentT>() {
      @Override
      public HasParentT apply(HasParentT hasParent) {
        return hasParent.getParent();
      }
    });
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  Iterable<CompositeT> nextLeaves(CompositeT current) {
    return iterate(current, new Function<CompositeT, CompositeT>() {
      @Override
      public CompositeT apply(CompositeT c) {
        return nextLeaf(c);
      }
    });
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  Iterable<CompositeT> prevLeaves(CompositeT current) {
    return iterate(current, new Function<CompositeT, CompositeT>() {
      @Override
      public CompositeT apply(CompositeT c) {
        return prevLeaf(c);
      }
    });
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  Iterable<CompositeT> nextNavOrder(final CompositeT current) {
    return iterate(current, new Function<CompositeT, CompositeT>() {
      @Override
      public CompositeT apply(CompositeT input) {
        return nextNavOrder(current, input);
      }
    });
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  Iterable<CompositeT> prevNavOrder(final CompositeT current) {
    return iterate(current, new Function<CompositeT, CompositeT>() {
      @Override
      public CompositeT apply(CompositeT input) {
        return prevNavOrder(current, input);
      }
    });
  }

  private static <CompositeT extends NavComposite<CompositeT>>
  CompositeT nextNavOrder(CompositeT start, CompositeT current) {
    CompositeT nextSibling = current.nextSibling();

    if (nextSibling != null) {
      return firstLeaf(nextSibling);
    }

    if (isNonCompositeChild(current)) return null;

    CompositeT parent = current.getParent();
    if (!isDescendant(parent, start)) return parent;
    return nextNavOrder(start, parent);
  }

  private static <CompositeT extends NavComposite<CompositeT>>
  CompositeT prevNavOrder(CompositeT start, CompositeT current) {
    CompositeT prevSibling = current.prevSibling();

    if (prevSibling != null) {
      return lastLeaf(prevSibling);
    }

    if (isNonCompositeChild(current)) return null;

    CompositeT parent = current.getParent();
    if (!isDescendant(parent, start)) return parent;
    return prevNavOrder(start, parent);
  }

  public static <CompositeT extends NavComposite<CompositeT>>
  boolean isBefore(CompositeT c1, CompositeT c2) {
    if (c1 == c2) return false;

    List<CompositeT> c1a = reverseAncestors(c1);
    List<CompositeT> c2a = reverseAncestors(c2);

    if (c1a.get(0) != c2a.get(0)) {
      throw new IllegalArgumentException("Items are in different trees");
    }

    int commonLength = Math.min(c1a.size(), c2a.size());
    for (int i = 1; i < commonLength; i++) {
      CompositeT first = c1a.get(i);
      CompositeT second = c2a.get(i);
      if (first != second) {
        return deltaBetween(first, second) > 0;
      }
    }

    throw new IllegalArgumentException("One parameter is an ancestor of the other");
  }

  private static <CompositeT extends NavComposite<CompositeT>>
  int deltaBetween(CompositeT c1, CompositeT c2) {
    CompositeT left = c1;
    CompositeT right = c1;
    int delta = 0;

    while (true) {
      if (left == c2) {
        return -delta;
      }
      if (right == c2) {
        return delta;
      }

      delta++;

      if (left != null) {
        left = left.prevSibling();
      }
      if (right != null) {
        right = right.nextSibling();
      }
    }
  }

  /**
   * @return Lowest common ancestor for the given nodes or {@code null} when they have no common ancestors.
   */
  public static <HasParentT extends HasParent<HasParentT>> HasParentT commonAncestor(HasParentT object1, HasParentT object2) {
    if (object1 == object2) {
      return object1;
    } else if (isDescendant(object1, object2)) {
      return object1;
    } else if (isDescendant(object2, object1)) {
      return object2;
    }

    Stack<HasParentT> stack1 = new Stack<>();
    Stack<HasParentT> stack2 = new Stack<>();
    for (HasParentT c : ancestorsFrom(object1)) {
      stack1.push(c);
    }
    for (HasParentT c : ancestorsFrom(object2)) {
      stack2.push(c);
    }

    if (stack1.isEmpty() || stack2.isEmpty()) {
      return null;
    } else {
      do {
        HasParentT pop1 = stack1.pop();
        HasParentT pop2 = stack2.pop();
        if (pop1 != pop2) {
          return pop1.getParent();
        }
      } while (!stack1.isEmpty() && !stack2.isEmpty());
      return null;
    }
  }

  public static <HasParentT extends HasParent<HasParentT>> HasParentT getClosestAncestor(HasParentT current,
      boolean acceptSelf, Predicate<HasParentT> p) {
    Iterable<HasParentT> ancestors = acceptSelf ? ancestorsFrom(current) : ancestors(current);
    for (HasParentT c : ancestors) {
      if (p.test(c)) {
        return c;
      }
    }
    return null;
  }

  public static <HasParentT extends HasParent<HasParentT>> boolean isDescendant(Object ancestor, HasParentT current) {
    return getClosestAncestor(current, true, Functions.<HasParentT>same(ancestor)) != null;
  }

  private static <HasParentT extends HasParent<HasParentT>> List<HasParentT> reverseAncestors(HasParentT c) {
    List<HasParentT> result = new ArrayList<>();
    collectReverseAncestors(c, result);
    return result;
  }

  private static <HasParentT extends HasParent<HasParentT>>
  void collectReverseAncestors(HasParentT c, List<HasParentT> result) {
    HasParentT parent = c.getParent();
    if (parent != null) {
      collectReverseAncestors(parent, result);
    }
    result.add(c);
  }

  static <ItemT> List<ItemT> toList(Iterable<ItemT> it) {
    List<ItemT> result = new ArrayList<>();
    for (ItemT i : it) {
      result.add(i);
    }
    return result;
  }

  public static <CompositeT extends Composite<CompositeT> & HasVisibility>
  boolean isLastChild(CompositeT v) {
    CompositeT parent = v.getParent();
    if (parent == null) return false;
    List<CompositeT> siblings = parent.children();
    int index = siblings.indexOf(v);
    for (CompositeT cv : siblings.subList(index + 1, siblings.size())) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <CompositeT extends Composite<CompositeT> & HasVisibility>
  boolean isFirstChild(CompositeT cell) {
    CompositeT parent = cell.getParent();
    if (parent == null) return false;
    List<CompositeT> siblings = parent.children();
    int index = siblings.indexOf(cell);

    for (CompositeT cv : siblings.subList(0, index)) {
      if (cv.visible().get()) return false;
    }
    return true;
  }

  public static <CompositeT extends Composite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT firstFocusable(CompositeT v) {
    return firstFocusable(v, true);
  }

  public static <CompositeT extends Composite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT firstFocusable(CompositeT v, boolean deepest) {
    for (CompositeT cv : v.children()) {
      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      CompositeT result = firstFocusable(cv);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;

    return null;
  }

  public static <CompositeT extends Composite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT lastFocusable(CompositeT c) {
    return lastFocusable(c, true);
  }

  public static <CompositeT extends Composite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT lastFocusable(CompositeT v, boolean deepest) {
    List<CompositeT> children = v.children();
    for (int i = children.size() - 1; i >= 0; i--) {
      CompositeT cv = children.get(i);

      if (!cv.visible().get()) continue;
      if (!deepest && cv.focusable().get()) return cv;

      CompositeT result = lastFocusable(cv, deepest);
      if (result != null) return result;
    }

    if (v.focusable().get()) return v;
    return null;
  }

  public static <HasParentT extends HasParent<HasParentT> & HasVisibility> boolean isVisible(HasParentT v) {
    return getClosestAncestor(v, true, Composites.<HasParentT>isInvisible()) == null;
  }

  public static <HasParentT extends HasParent<HasParentT> & HasFocusability> HasParentT focusableParent(HasParentT v) {
    return getClosestAncestor(v, false, Composites.<HasParentT>isFocusable());
  }

  public static <HasParentT extends HasParent<HasParentT> & HasFocusability & HasVisibility>
  boolean isFocusable(HasParentT v) {
    return v.focusable().get() && isVisible(v);
  }

  public static <CompositeT extends NavComposite<CompositeT>> CompositeT next(CompositeT c, Predicate<CompositeT> p) {
    for (CompositeT next : nextNavOrder(c)) {
      if (p.test(next)) return next;
    }
    return null;
  }

  public static <CompositeT extends NavComposite<CompositeT>> CompositeT prev(CompositeT v, Predicate<CompositeT> p) {
    for (CompositeT prev : prevNavOrder(v)) {
      if (p.test(prev)) return prev;
    }
    return null;
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT nextFocusable(CompositeT v) {
    for (CompositeT cv : nextNavOrder(v)) {
      if (isFocusable(cv)) return cv;
    }
    return null;
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility>
  CompositeT prevFocusable(CompositeT v) {
    for (CompositeT cv : prevNavOrder(v)) {
      if (isFocusable(cv)) return cv;
    }
    return null;
  }

  static <ValueT> Iterable<ValueT> iterate(ValueT initial, Function<ValueT, ValueT> trans) {
    return iterateFrom(trans.apply(initial), trans);
  }

  private static <ValueT> Iterable<ValueT> iterateFrom(final ValueT initial, final Function<ValueT, ValueT> trans) {
    return new Iterable<ValueT>() {
      @Override
      public Iterator<ValueT> iterator() {
        return new Iterator<ValueT>() {
          private ValueT myCurrent = initial;

          @Override
          public boolean hasNext() {
            return myCurrent != null;
          }

          @Override
          public ValueT next() {
            if (myCurrent == null) {
              throw new NoSuchElementException();
            }
            ValueT result = myCurrent;
            myCurrent = trans.apply(myCurrent);
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

  /**
   * Returns a list that includes all nodes that have some parent strictly between
   * some parents of {@code from} and {@code from}.
   */
  public static <CompositeT extends NavComposite<CompositeT>>
      List<CompositeT> allBetween(CompositeT from, CompositeT to) {
    List<CompositeT> res = new ArrayList<>();

    if (to != from) {
      includeClosed(from, to, res);
    }

    return res;
  }

  private static <CompositeT extends NavComposite<CompositeT>>
      void includeClosed(CompositeT left, CompositeT to, List<CompositeT> res) {
    for (CompositeT next = left.nextSibling(); next != null; next = next.nextSibling()) {
      if (includeOpen(next, to, res)) {
        return;
      }
    }

    if (left.getParent() == null) {
      throw new IllegalArgumentException("Right bound not found in left's bound hierarchy. to=" + to);
    }

    includeClosed(left.getParent(), to, res);
  }

  private static <CompositeT extends NavComposite<CompositeT>>
      boolean includeOpen(CompositeT node, CompositeT to, List<CompositeT> res) {

    if (node == to) {
      return true;
    }

    for (CompositeT c : node.children()) {
      if (includeOpen(c, to, res)) {
        return true;
      }
    }

    res.add(node);
    return false;
  }

  //has bounds
  public static <CompositeT extends HasBounds> boolean isAbove(CompositeT upper, CompositeT lower) {
    return ourWithBounds.isAbove(upper, lower);
  }

  public static <CompositeT extends HasBounds> boolean isBelow(CompositeT lower, CompositeT upper) {
    return ourWithBounds.isBelow(lower, upper);
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility & HasBounds>
  CompositeT homeElement(CompositeT cell) {
    return ourWithBounds.homeElement(cell);
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility & HasBounds>
  CompositeT endElement(CompositeT cell) {
    return ourWithBounds.endElement(cell);
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility & HasBounds>
  CompositeT upperFocusable(CompositeT v, int xOffset) {
    return ourWithBounds.upperFocusable(v, xOffset);
  }

  public static <CompositeT extends NavComposite<CompositeT> & HasFocusability & HasVisibility & HasBounds>
  CompositeT lowerFocusable(CompositeT v, int xOffset) {
    return ourWithBounds.lowerFocusable(v, xOffset);
  }

  public static <CompositeT extends Composite<CompositeT>> Iterable<CompositeT> preOrderTraversal(CompositeT root) {
    return TreeTraverser.using(new com.google.common.base.Function<CompositeT, Iterable<CompositeT>>() {
      @Override
      public Iterable<CompositeT> apply(CompositeT input) {
        return input == null ? Collections.<CompositeT>emptyList() : input.children();
      }
    }).preOrderTraversal(root);
  }

  private Composites() {
  }
}