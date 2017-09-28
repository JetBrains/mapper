package jetbrains.jetpad.model.util;

public class TypedKeyListMap extends ListMap<TypedKey<?>, Object> implements TypedKeyContainer {
  @Override
  public <T> T get(TypedKey<T> key) {
    @SuppressWarnings("unchecked")
    T result = (T) super.get(key);
    return result;
  }

  @Override
  public <T> T put(TypedKey<T> key, T value) {
    @SuppressWarnings("unchecked")
    T result = (T) super.put(key, value);
    return result;
  }

  @Override
  public boolean contains(TypedKey<?> key) {
    return containsKey(key);
  }
}
