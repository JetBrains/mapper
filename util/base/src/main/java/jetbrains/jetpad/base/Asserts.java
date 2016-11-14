package jetbrains.jetpad.base;

import jetbrains.jetpad.base.function.Predicate;

public class Asserts {
  public static void assertSuccess(Async<Void> async) {
    assertSuccessValue(null, async);
  }

  public static <T> void assertSuccessValue(T expected, Async<T> async) {
    T value = Asyncs.get(async);
    if (value == null) {
      if (expected != null) {
        throw new AssertionError("Expected: " + expected + ", but got: " + value);
      }
    } else if (!value.equals(expected)) {
      throw new AssertionError("Expected: " + expected + ", but got: " + value);
    }
  }

  public static <T> void assertSuccess(Predicate<T> assertion, Async<T> async) {
    T value = Asyncs.get(async);
    if (!assertion.test(value)) {
      throw new AssertionError("succes value failed assertion: " + value);
    }
  }

  public static void assertFailure(Class<? extends Throwable> expected, Async<?> async) {
    try {
      Asyncs.get(async);
      throw new AssertionError("Async expected to fail: " + async);
    } catch (Throwable t) {
      if (!expected.isAssignableFrom(t.getClass())) {
        throw new AssertionError("Async failed with unexpected exception expected=" + expected + ", exception=" + t);
      }
    }
  }

  private Asserts() { }
}
