package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThreadSafeAsync;
import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunnableWithAsync<ResultT> implements Runnable, Async<ResultT> {
  private final Supplier<ResultT> mySupplier;
  private final ThreadSafeAsync<ResultT> myAsync;
  private final AtomicBoolean myFulfilled = new AtomicBoolean(false);

  static RunnableWithAsync<Void> fromRunnable(final Runnable r) {
    final ThreadSafeAsync<Void> async = new ThreadSafeAsync<>();
    Supplier<Void> s = new Supplier<Void>() {
      @Override
      public Void get() {
        r.run();
        return null;
      }
    };
    return new RunnableWithAsync<>(s, async);
  }

  static <ResT> RunnableWithAsync<ResT> fromSupplier(final Supplier<ResT> s) {
    return new RunnableWithAsync<>(s, new ThreadSafeAsync<ResT>());
  }

  private RunnableWithAsync(Supplier<ResultT> r, ThreadSafeAsync<ResultT> async) {
    mySupplier = r;
    myAsync = async;
  }

  @Override
  public void run() {
    try {
      ResultT resultT = mySupplier.get();
      if (myFulfilled.compareAndSet(false, true)) {
        myAsync.success(resultT);
      }
    } catch (Throwable t) {
      if (myFulfilled.compareAndSet(false, true)) {
        myAsync.failure(t);
      }
      throw t;
    }
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
