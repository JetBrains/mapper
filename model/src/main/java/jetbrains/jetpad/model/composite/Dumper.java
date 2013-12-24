package jetbrains.jetpad.model.composite;

import com.google.common.base.Strings;

public class Dumper {
  public static <CompositeT extends Composite<CompositeT>> String dump(CompositeT c) {
    StringBuilder result = new StringBuilder();
    dump(c, result, 0);
    return result.toString();
  }

  private static <CompositeT extends Composite<CompositeT>> void dump(CompositeT c, StringBuilder result, int indent) {
    result.append(Strings.repeat("  ", indent)).append(c.toString()).append("\n");
    for (CompositeT child : c.children()) {
      dump(child, result, indent + 1);
    }
  }

}
