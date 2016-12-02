package jetbrains.jetpad.base.props;

public class AppProperties {
  private static PropertyProvider ourPropertyProvider = new EmptyPropertyProvider();

  public static void setProvider(PropertyProvider provider) {
    ourPropertyProvider = provider;
  }

  public static String get(String key, String defaultValue) {
    String value = get(key);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public static String get(String key) {
    return ourPropertyProvider.get(key);
  }
}
