package jetbrains.jetpad.model.util;

public final class TypedKeys {

  public static final TypedKey<Boolean> BOOLEAN = create("Boolean");
  public static final TypedKey<Integer> INTEGER = create("Integer");
  public static final TypedKey<String> STRING = create("String");

  public static <T> TypedKey<T> create(final String name) {
    return new TypedKey<T>() {
      @Override
      public String toString() {
        return name;
      }
    };
  }

  private TypedKeys() {
  }

}
