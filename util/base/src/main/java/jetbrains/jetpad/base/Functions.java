package jetbrains.jetpad.base;

import java.util.function.Supplier;

public final class Functions {
  private Functions() { }

  public static <T> Supplier<T> constant(T value) {
    return () -> value;
  }

  public static <T> Supplier<T> memorize(Supplier<T> supplier) {
    return new Memo<>(supplier);
  }

  private static class Memo<T> implements Supplier<T> {
    private final Supplier<T> mySupplier;
    private T myCachedValue = null;
    private boolean myCached = false;

    public Memo(Supplier<T> supplier) {
      mySupplier = supplier;
    }

    @Override
    public T get() {
      if (!myCached) {
        myCachedValue = mySupplier.get();
        myCached = true;
      }
      return myCachedValue;
    }
  }
}
