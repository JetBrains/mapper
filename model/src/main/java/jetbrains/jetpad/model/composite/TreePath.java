package jetbrains.jetpad.model.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreePath<CompositeT extends Composite<CompositeT>> {
  private List<Integer> myPath = new ArrayList<Integer>();

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
    TreePath path = (TreePath) o;
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