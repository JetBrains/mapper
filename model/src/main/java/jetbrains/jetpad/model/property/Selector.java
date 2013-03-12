package jetbrains.jetpad.model.property;

public interface Selector<SourceT, TargetT> {
  TargetT select(SourceT source);
}
