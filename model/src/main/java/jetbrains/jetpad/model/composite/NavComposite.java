package jetbrains.jetpad.model.composite;

public interface NavComposite<CompositeT extends NavComposite<CompositeT>> extends Composite<CompositeT> {
  CompositeT nextSibling();
  CompositeT prevSibling();
}
