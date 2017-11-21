package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.function.Supplier;

import static jetbrains.jetpad.base.AsyncMatchers.failed;
import static jetbrains.jetpad.base.AsyncMatchers.result;
import static jetbrains.jetpad.base.AsyncMatchers.unfinished;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

final class EdtTestUtil {
  private static final int VALUE = 42;
  static Supplier<Integer> getDefaultSupplier() {
    return new Supplier<Integer>() {
      @Override
      public Integer get() {
        return VALUE;
      }
    };
  }

  static void killAndAssertFailure(EdtManager manager, Async<?>... asyncs) {
    for (Async<?> async : asyncs) {
      assertThat(async, unfinished());
    }
    manager.kill();
    for (Async<?> async : asyncs) {
      assertThat(async, failed());
    }
  }

  static void assertAsyncFulfilled(EventDispatchThread edt, Runnable flush) {
    Async<Integer> async = edt.schedule(new Supplier<Integer>() {
      @Override
      public Integer get() {
        return VALUE;
      }
    });
    flush.run();
    assertThat(async, result(is(VALUE)));
  }

  static void assertAsyncRejected(final EventDispatchThread edt, final Runnable flush) {
    final Value<Async<Integer>> asyncValue = new Value<>();
    ThrowableHandlers.asInProduction(new Runnable() {
      @Override
      public void run() {
        Async<Integer> async = edt.schedule(new Supplier<Integer>() {
          @Override
          public Integer get() {
            throw new RuntimeException();
          }
        });
        flush.run();
        asyncValue.set(async);
      }
    });
    assertThat(asyncValue.get(), failed());
  }

  private EdtTestUtil() {
  }
}
