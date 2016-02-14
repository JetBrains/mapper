package jetbrains.jetpad.mapper.gwt.elemental;

import elemental.dom.Node;

import java.util.AbstractList;
import java.util.List;

public class DomUtil {
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
}
