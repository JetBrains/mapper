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

import jetbrains.jetpad.model.collections.list.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreePath<CompositeT extends Composite<CompositeT>> implements Comparable<TreePath<CompositeT>>{
  private List<Integer> myPath = new ArrayList<>();

  public TreePath(CompositeT composite) {
    this(composite, null);
  }

  public TreePath(CompositeT from, CompositeT to) {
    CompositeT current = from;
    while (current != null && current != to) {
      CompositeT parent = current.getParent();
      if (parent != null) {
        int index = parent.children().indexOf(current);
        if (index == -1) {
          throw new IllegalStateException();
        }
        myPath.add(index);
      }
      current = parent;
    }
    if (current != to) {
      throw new IllegalStateException();
    }
    Collections.reverse(myPath);
  }

  private TreePath(List<Integer> path) {
    myPath.addAll(path);
  }

  public CompositeT get(CompositeT root) {
    if (!isValid(root)) {
      throw new IllegalStateException("Invalid context");
    }

    CompositeT current = root;
    for (Integer i : myPath) {
      current = current.children().get(i);
    }
    return current;
  }

  public boolean isValid(CompositeT root) {
    CompositeT current = root;
    for (Integer i : myPath) {
      ObservableList<CompositeT> children = current.children();
      if (i >= children.size()) {
        return false;
      }
      current = children.get(i);
    }
    return true;
  }

  public int getLastIndex() {
    if (myPath.isEmpty()) {
      throw new IllegalStateException();
    }
    return myPath.get(myPath.size() - 1);
  }

  public TreePath<CompositeT> getParent() {
    if (myPath.isEmpty()) {
      throw new IllegalStateException();
    }

    return new TreePath<>(myPath.subList(0, myPath.size() - 1));
  }

  public TreePath<CompositeT> getChild(int index) {
    List<Integer> newPath = new ArrayList<>();
    newPath.addAll(myPath);
    newPath.add(index);
    return new TreePath<>(newPath);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TreePath)) return false;
    TreePath<?> path = (TreePath<?>) o;
    return myPath.equals(path.myPath);
  }

  @Override
  public int hashCode() {
    return myPath.hashCode();
  }

  @Override
  public int compareTo(TreePath<CompositeT> o) {
    int maxIndex = Math.min(myPath.size(), o.myPath.size());
    for (int i = 0; i < maxIndex; i++) {
      int delta = myPath.get(i) - o.myPath.get(i);
      if (delta != 0) return delta;
    }
    return myPath.size() - o.myPath.size();
  }

  @Override
  public String toString() {
    return myPath.toString();
  }
}