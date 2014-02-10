/*
 * Copyright 2012-2014 JetBrains s.r.o
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
import java.util.Collections;
import java.util.List;

public class TreePath<CompositeT extends Composite<CompositeT>> {
  private List<Integer> myPath = new ArrayList<>();

  public TreePath(CompositeT composite) {
    CompositeT current = composite;
    while (current != null) {
      CompositeT parent = current.parent().get();
      if (parent != null) {
        int index = parent.children().indexOf(current);
        if (index == -1) throw new IllegalStateException();
        myPath.add(index);
      }
      current = parent;
    }

    Collections.reverse(myPath);
  }

  public CompositeT get(CompositeT root) {
    CompositeT current = root;
    for (Integer i : myPath) {
      current = current.children().get(i);
    }
    return current;
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
  public String toString() {
    return myPath.toString();
  }
}