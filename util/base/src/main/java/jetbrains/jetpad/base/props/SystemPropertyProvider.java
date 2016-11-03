package jetbrains.jetpad.base.props;

public class SystemPropertyProvider implements PropertyProvider {
  private String myPrefix;

  public SystemPropertyProvider(String myPrefix) {
    this.myPrefix = myPrefix;
  }

  @Override
  public String get(String key) {
    return System.getProperty(myPrefix + "." + key);
  }
}
