package jetbrains.jetpad.base.props;

public class Props {
  private static PropertyProvider ourPropertyProvider = new EmptyPropertyProvider();

  public static void setProvider(PropertyProvider provider) {
    ourPropertyProvider = provider;
  }

  public static String get(String key) {
    return ourPropertyProvider.get(key);
  }
}
