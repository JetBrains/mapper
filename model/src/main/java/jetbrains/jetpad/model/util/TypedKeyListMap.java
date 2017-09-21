package jetbrains.jetpad.model.util;

public class TypedKeyListMap extends ListMap<TypedKey<?>, Object> implements TypedKeyContainer {
  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(TypedKey<T> key) {
    return (T) super.get(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T put(TypedKey<T> key, T value) {
    return (T) super.put(key, value);
  }
}
