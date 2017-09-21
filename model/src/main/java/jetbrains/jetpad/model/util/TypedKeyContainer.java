package jetbrains.jetpad.model.util;

public interface TypedKeyContainer {
  <T> T get(TypedKey<T> key);
  <T> T put(TypedKey<T> key, T value);
}
