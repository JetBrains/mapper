package jetbrains.jetpad.base;

import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Predicate;

public class Asserts {
  public static <T> void assertSuccess(Async<T> async) {
    assertSuccess(new Predicate<T>() {
      @Override
      public boolean test(T value) {
        return true;
      }
    }, async);
  }

  public static <T> void assertSuccessValue(T expected, Async<T> async) {
    T value = getResultValue(async);
    if (value == null) {
      if (expected != null) {
        throw new AssertionError("Expected: " + expected + ", but got: " + value);
      }
    } else if (!value.equals(expected)) {
      throw new AssertionError("Expected: " + expected + ", but got: " + value);
    }
  }

  public static <T> void assertSuccess(Predicate<T> assertion, Async<T> async) {
    T value = getResultValue(async);
    if (!assertion.test(value)) {
      throw new AssertionError("succes value failed assertion: " + value);
    }
  }

  public static void assertFailure(Async<?> async) {
    assertFailure(Throwable.class, async);
  }

  public static void assertFailure(Class<? extends Throwable> expected, Async<?> async) {
    AsyncResult<?> result = getResult(async);
    if (result.state != AsyncState.FAILED) {
      throw new AssertionError("Async expected to succeed: async=" + async + ", state=" + result.state);
    }
    Throwable t = result.error;
    if (!expected.isAssignableFrom(t.getClass())) {
      throw new AssertionError("Async failed with unexpected exception expected=" + expected + ", exception=" + t);
    }
  }

  public static <T> T getResultValue(Async<T> async) {
    AsyncResult<T> result = getResult(async);
    if (result.state != AsyncState.SUCCEEDED) {
      throw new AssertionError("Async expected to succeed: async=" + async + ", state=" + result.state);
    }
    return result.value;
  }

  private static <T> AsyncResult<T> getResult(Async<T> async) {
    final Value<AsyncResult<T>> resultValue = new Value<>(new AsyncResult<T>(AsyncState.UNFINISHED, null, null));
    async.onResult(new Consumer<T>() {
      @Override
      public void accept(T value) {
        resultValue.set(new AsyncResult<>(AsyncState.SUCCEEDED, value, null));
      }
    }, new Consumer<Throwable>() {
      @Override
      public void accept(Throwable throwable) {
        resultValue.set(new AsyncResult<T>(AsyncState.FAILED, null, throwable));
      }
    });
    return resultValue.get();
  }

  private Asserts() { }

  private static class AsyncResult<T> {
    private final AsyncState state;
    private final T value;
    private final Throwable error;

    private AsyncResult(AsyncState state, T value, Throwable error) {
      this.state = state;
      this.value = value;
      this.error = error;
    }
  }

  private enum AsyncState {
    UNFINISHED,
    SUCCEEDED,
    FAILED
  }
}
