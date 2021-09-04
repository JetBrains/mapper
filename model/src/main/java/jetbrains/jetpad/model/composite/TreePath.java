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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreePath<CompositeT extends Composite<CompositeT>> implements Comparable<TreePath<CompositeT>> {

  public static <CompositeT extends Composite<CompositeT>> void sort(List<? extends CompositeT> composites) {
    final Map<CompositeT, TreePath<CompositeT>> paths = new HashMap<>();
    for (CompositeT composite : composites) {
      paths.put(composite, new TreePath<>(composite));
    }
    Collections.sort(composites, new Comparator<CompositeT>() {
      @Override
      public int compare(CompositeT composite1, CompositeT composite2) {
        return paths.get(composite1).compareTo(paths.get(composite2));
      }
    });
  }

  public static <CompositeT extends Composite<CompositeT>> TreePath<CompositeT> deserialize(String text) {
    if (text.isEmpty()) {
      return new TreePath<>(Collections.<Integer>emptyList());
    }
    String[] split = text.split(",");
    List<Integer> path = new ArrayList<>(split.length);
    for (String p : split) {
      try {
        path.add(Integer.parseInt(p));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Failed to parse TreePath: " + text, e);
      }
    }
    return new TreePath<>(path);
  }

  private List<Integer> myPath = new LinkedList<>();

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
      List<CompositeT> children = current.children();
      if (i >= children.size()) {
        return false;
      }
      current = children.get(i);
    }
    return true;
  }

  public boolean isEmpty() {
    return myPath.isEmpty();
  }

  public int getLastIndex() {
    if (myPath.isEmpty()) {
      throw new IllegalStateException();
    }
    return myPath.getLast();
  }

  public TreePath<CompositeT> getParent() {
    if (myPath.isEmpty()) {
      throw new IllegalStateException();
    }

    ArrayList<Integer> treePathList = new ArrayList<>(myPath);
    return new TreePath<>(treePathList.subList(0, treePathList.size() - 1));
  }

  public TreePath<CompositeT> getChild(int index) {
    List<Integer> newPath = new ArrayList<>(myPath);
    newPath.add(index);
    return new TreePath<>(newPath);
  }

  public String serialize() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int p : myPath) {
      if (!first) {
        sb.append(',');
      }
      sb.append(p);
      first = false;
    }
    return sb.toString();
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
