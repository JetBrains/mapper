package jetbrains.jetpad.model.children;

import jetbrains.jetpad.model.property.ReadableProperty;

import java.util.List;

public interface Composite<CompositeT extends Composite<CompositeT>> {
  ReadableProperty<CompositeT> parent();
  List<CompositeT> children();
}
