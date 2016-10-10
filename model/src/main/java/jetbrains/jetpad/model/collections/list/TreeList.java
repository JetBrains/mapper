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
package jetbrains.jetpad.model.collections.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;

public class TreeList<T> extends AbstractList<T> {
  private AvlTree<T> myTree;

  @Override
  public T get(int index) {
    if (myTree == null) {
      throw new IndexOutOfBoundsException();
    }
    return myTree.get(index);
  }

  @Override
  public int size() {
    if (myTree == null) {
      return 0;
    }
    return myTree.mySize;
  }

  @Override
  public T set(int index, T element) {
    if (myTree == null) {
      throw new IndexOutOfBoundsException();
    }
    T oldValue = myTree.get(index);
    myTree = myTree.set(index, element);
    return oldValue;
  }

  @Override
  public void add(int index, T element) {
    if (myTree == null) {
      if (index != 0) {
        throw new IndexOutOfBoundsException();
      }
      myTree = new AvlTree<>(element);
    } else {
      myTree = myTree.insert(index, element);
    }
  }

  @Override
  public T remove(int index) {
    if (myTree == null) {
      throw new IndexOutOfBoundsException();
    }
    T oldValue = myTree.get(index);
    myTree = myTree.remove(index);
    return oldValue;
  }

  public void check() {
    if (myTree == null) return;
    myTree.check();
  }

  private static class AvlTree<T> {
    private final T myValue;
    private final AvlTree<T> myLeft;
    private final AvlTree<T> myRight;
    private final int myHeight;
    private final int mySize;

    AvlTree(T value) {
      this(null, null, value);
    }

    AvlTree(AvlTree<T> left, AvlTree<T> right, T value) {
      myLeft = left;
      myRight = right;
      myValue = value;
      myHeight = Math.max(height(left), height(right)) + 1;
      mySize = 1 + size(left) + size(right);
    }

    AvlTree<T> rotateRight() {
      if (myLeft == null) {
        throw new IllegalStateException();
      }

      return new AvlTree<>(myLeft.myLeft, new AvlTree<>(myLeft.myRight, myRight, myValue), myLeft.myValue);
    }

    AvlTree<T> rotateLeft() {
      if (myRight == null) {
        throw new IllegalStateException();
      }

      return new AvlTree<>(new AvlTree<>(myLeft, myRight.myLeft, myValue), myRight.myRight, myRight.myValue);
    }

    T get(int index) {
      int leftSize = size(myLeft);

      if (index < leftSize) {
        if (myLeft == null) {
          throw new IndexOutOfBoundsException();
        }
        return myLeft.get(index);
      } else if (index == leftSize) {
        return myValue;
      } else {
        if (myRight == null) {
          throw new IndexOutOfBoundsException();
        }
        return myRight.get(index - leftSize - 1);
      }
    }

    AvlTree<T> set(int index, T value) {
      int leftSize = size(myLeft);

      if (index < leftSize) {
        if (myLeft == null) {
          throw new IndexOutOfBoundsException();
        }
        return new AvlTree<>(myLeft.set(index, value), myRight, myValue);
      } else if (index == leftSize) {
        return new AvlTree<T>(myLeft, myRight, value);
      } else {
        if (myRight == null) {
          throw new IndexOutOfBoundsException();
        }
        return new AvlTree<>(myLeft, myRight.set(index - 1 - leftSize, value), myValue);
      }
    }


    AvlTree<T> insert(int index, T value) {
      int leftSize = size(myLeft);
      if (index <= leftSize) {
        AvlTree<T> unbalanced;
        if (myLeft == null) {
          if (index == 0) {
            unbalanced = new AvlTree<>(new AvlTree<>(value), myRight, myValue);
          } else {
            throw new IndexOutOfBoundsException();
          }
        } else {
          unbalanced = new AvlTree<T>(myLeft.insert(index, value), myRight, myValue);
        }
        return balanceLeft(unbalanced);
      } else {
        AvlTree<T> unbalanced;
        if (myRight == null) {
          if (index == leftSize + 1) {
            unbalanced = new AvlTree<T>(myLeft, new AvlTree<T>(value), myValue);
          } else {
            throw new IndexOutOfBoundsException();
          }
        } else {
          unbalanced = new AvlTree<T>(myLeft, myRight.insert(index - 1 - leftSize, value), myValue);
        }
        return balanceRight(unbalanced);
      }
    }

    private AvlTree<T> balanceRight(AvlTree<T> result) {
      int delta = height(result.myRight) - height(result.myLeft);
      if (delta > 1) {
        if (height(result.myRight.myLeft) <= height(result.myRight.myRight)) {
          return result.rotateLeft();
        } else {
          return new AvlTree<T>(result.myLeft, result.myRight.rotateRight(), result.myValue).rotateLeft();
        }
      }
      return result;
    }

    private AvlTree<T> balanceLeft(AvlTree<T> result) {
      int delta = height(result.myLeft) - height(result.myRight);
      if (delta > 1) {
        if (height(result.myLeft.myLeft) >= height(result.myLeft.myRight)) {
          return result.rotateRight();
        } else {
          return new AvlTree<>(result.myLeft.rotateLeft(), result.myRight, result.myValue).rotateRight();
        }
      }

      return result;
    }

    AvlTree<T> remove(int index) {
      int leftSize = size(myLeft);

      if (index == leftSize) {
        if (myLeft == null) return myRight;
        if (myRight == null) return myLeft;

        T newVal = myRight.get(0);
        return balanceLeft(new AvlTree<>(myLeft, myRight.remove(0), newVal));
      } else if (index < leftSize) {
        if (myLeft == null) {
          throw new IndexOutOfBoundsException();
        }
        return balanceRight(new AvlTree<>(myLeft.remove(index), myRight, myValue));
      } else {
        if (myRight == null) {
          throw new IndexOutOfBoundsException();
        }
        return balanceLeft(new AvlTree<T>(myLeft, myRight.remove(index - leftSize - 1), myValue));
      }
    }

    void check() {
      if (Math.abs(height(myLeft) - height(myRight)) > 1) {
        throw new IllegalStateException();
      }
      if (myLeft != null) {
        myLeft.check();
      }
      if (myRight != null) {
        myRight.check();
      }
    }

    private int height(AvlTree tree) {
      if (tree == null) return 0;
      return tree.myHeight;
    }

    private int size(AvlTree tree) {
      if (tree == null) return 0;
      return tree.mySize;
    }

    @Override
    public int hashCode() {
      int result = 0;
      if (myLeft != null) {
        result += 31 * myLeft.hashCode();
      }
      if (myRight != null) {
        result += 71 * myRight.hashCode();
      }

      result += myValue == null ? 0 : myValue.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof AvlTree)) {
        return false;
      }

      AvlTree otherTree = (AvlTree) obj;

      return otherTree.myValue == myValue && Objects.equals(otherTree.myLeft, myLeft) && Objects.equals(otherTree.myRight, myRight);
    }
  }
}