package jetbrains.jetpad.base;

public class Asserts {
  public static void assertSuccess(Async<Void> async) {
    assertSuccess(async, null);
  }

  public static <T> void assertSuccess(Async<T> async, T expected) {
    T value = Asyncs.get(async);
    if (value == null) {
      if (expected != null) {
        throw new AssertionError("Expected: " + expected + ", but got: " + value);
      }
    } else if (!value.equals(expected)) {
      throw new AssertionError("Expected: " + expected + ", but got: " + value);
    }
  }

  public static void assertFailure(Async<?> async, Class<? extends Throwable> expected) {
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
