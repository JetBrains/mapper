package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.function.Supplier;

public abstract class DefaultAsyncEdt implements EventDispatchThread {
  @Override
  public final Async<Void> schedule(Runnable r) throws EdtException {
    return asyncSchedule(RunnableWithAsync.fromRunnable(r));
  }

  @Override
  public final <ResultT> Async<ResultT> schedule(Supplier<ResultT> s) throws EdtException {
    return asyncSchedule(RunnableWithAsync.fromSupplier(s));
  }

  @Override
  public <ResultT> Async<ResultT> flatSchedule(Supplier<Async<ResultT>> s) throws EdtException {
    return asyncSchedule(RunnableWithAsync.fromAsyncSupplier(s));
  }

  protected abstract <ResultT> Async<ResultT> asyncSchedule(RunnableWithAsync<ResultT> runnableWithAsync);
}
