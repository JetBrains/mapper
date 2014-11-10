package jetbrains.jetpad.base;

public class ThreadSafeSimpleAsync<ItemT> implements Async<ItemT> {
  private final SimpleAsync<ItemT> myAsync;

  public ThreadSafeSimpleAsync() {
    myAsync = new SimpleAsync<>();
  }

  @Override
  public Registration onSuccess(Handler<? super ItemT> successHandler) {
    synchronized (myAsync) {
      return myAsync.onSuccess(successHandler);
    }
  }

  @Override
  public Registration onResult(Handler<? super ItemT> successHandler, Handler<Throwable> failureHandler) {
    synchronized (myAsync) {
      return myAsync.onResult(successHandler, failureHandler);
    }
  }

  @Override
  public Registration onFailure(Handler<Throwable> failureHandler) {
    synchronized (myAsync) {
      return myAsync.onFailure(failureHandler);
    }
  }

  public void success(ItemT item) {
    synchronized (myAsync) {
      myAsync.success(item);
    }
  }

  public void failure(Throwable throwable) {
    synchronized (myAsync) {
      myAsync.failure(throwable);
    }
  }
}
