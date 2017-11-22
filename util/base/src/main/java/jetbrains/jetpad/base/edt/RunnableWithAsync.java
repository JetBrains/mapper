package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThreadSafeAsync;
import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunnableWithAsync<ResultT> implements Runnable, Async<ResultT> {
  private final AtomicBoolean myFulfilled;
  private final Runnable myAction;
  private final ThreadSafeAsync<ResultT> myAsync;

  static RunnableWithAsync<Void> fromRunnable(final Runnable r) {
    Supplier<Void> s = new Supplier<Void>() {
      @Override
      public Void get() {
        r.run();
        return null;
      }
    };
    return fromSupplier(s);
  }

  static <ResT> RunnableWithAsync<ResT> fromSupplier(final Supplier<ResT> s) {
    return plain(s);
  }

  static <ResT> RunnableWithAsync<ResT> fromAsyncSupplier(final Supplier<Async<ResT>> s) {
    return async(s);
  }

  private static <T> Runnable fromPlainSupplier(final Supplier<T> supplier, final ThreadSafeAsync<T> async,
      final AtomicBoolean fulfilled) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          T resultT = supplier.get();
          if (fulfilled.compareAndSet(false, true)) {
            async.success(resultT);
          }
        } catch (Throwable t) {
          if (fulfilled.compareAndSet(false, true)) {
            async.failure(t);
          }
          throw t;
        }
      }
    };
  }

  private static <T> Runnable fromAsyncSupplier(final Supplier<Async<T>> supplier, final ThreadSafeAsync<T> async,
      final AtomicBoolean fulfilled) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          Async<T> resultT = supplier.get();
          if (fulfilled.compareAndSet(false, true)) {
            Asyncs.delegate(resultT, async);
          }
        } catch (Throwable t) {
          if (fulfilled.compareAndSet(false, true)) {
            async.failure(t);
          }
          throw t;
        }
      }
    };
  }

  private static <ResT> RunnableWithAsync<ResT> plain(Supplier<ResT> s) {
    ThreadSafeAsync<ResT> async = new ThreadSafeAsync<>();
    AtomicBoolean fulfilled = new AtomicBoolean();
    return new RunnableWithAsync<>(fromPlainSupplier(s, async, fulfilled), async, fulfilled);
  }

  private static <ResT> RunnableWithAsync<ResT> async(Supplier<Async<ResT>> s) {
    ThreadSafeAsync<ResT> async = new ThreadSafeAsync<>();
    AtomicBoolean fulfilled = new AtomicBoolean();
    return new RunnableWithAsync<>(fromAsyncSupplier(s, async, fulfilled), async, fulfilled);
  }

  private RunnableWithAsync(Runnable action, ThreadSafeAsync<ResultT> async, AtomicBoolean fulfilled) {
    myAction = action;
    myAsync = async;
    myFulfilled = fulfilled;
  }

  @Override
  public void run() {
    myAction.run();
  }

  @Override
  public Registration onSuccess(Consumer<? super ResultT> successHandler) {
    return myAsync.onSuccess(successHandler);
  }

  @Override
  public Registration onResult(Consumer<? super ResultT> successHandler, Consumer<Throwable> failureHandler) {
    return myAsync.onResult(successHandler, failureHandler);
  }

  @Override
  public Registration onFailure(Consumer<Throwable> failureHandler) {
    return myAsync.onFailure(failureHandler);
  }

  @Override
  public <ResultT1> Async<ResultT1> map(Function<? super ResultT, ? extends ResultT1> success) {
    return myAsync.map(success);
  }

  @Override
  public <ResultT1> Async<ResultT1> flatMap(Function<? super ResultT, Async<ResultT1>> success) {
    return myAsync.flatMap(success);
  }

  void fail() {
    if (myFulfilled.compareAndSet(false, true)) {
      myAsync.failure(new RuntimeException("Intentionally failed"));
    }
  }
}
