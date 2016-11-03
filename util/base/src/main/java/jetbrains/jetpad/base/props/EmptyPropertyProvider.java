package jetbrains.jetpad.base.props;

public class EmptyPropertyProvider implements PropertyProvider {
  @Override
  public String get(String key) {
    return null;
  }
}
