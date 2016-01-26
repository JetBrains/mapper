package jetbrains.jetpad.mapper;

public class SimpleMapping<SourceT, TargetT> implements Mapping<SourceT, TargetT> {
  private final SourceT mySource;
  private final TargetT myTarget;

  public SimpleMapping(SourceT source, TargetT target) {
    mySource = source;
    myTarget = target;
  }

  @Override
  public SourceT getSource() {
    return mySource;
  }

  @Override
  public TargetT getTarget() {
    return myTarget;
  }
}
