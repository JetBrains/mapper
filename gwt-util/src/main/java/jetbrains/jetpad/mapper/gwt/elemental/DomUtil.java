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
package jetbrains.jetpad.mapper.gwt.elemental;

import elemental.dom.Node;

import java.util.AbstractList;
import java.util.List;

public final class DomUtil {
  public static List<Node> nodeChildren(final Node n) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return n.getChildNodes().item(index);
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        Node child = get(index);
        n.replaceChild(child, element);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        if (index == size()) {
          n.appendChild(element);
        } else {
          Node prev = get(index);
          n.insertBefore(prev, element);
        }
      }

      @Override
      public Node remove(int index) {
        Node child = n.getChildNodes().item(index);
        n.removeChild(child);
        return child;
      }

      @Override
      public int size() {
        return n.getChildNodes().getLength();
      }
    };
  }

  private DomUtil() {
  }
}