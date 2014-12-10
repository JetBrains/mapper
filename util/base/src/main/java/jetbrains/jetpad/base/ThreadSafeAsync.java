package jetbrains.jetpad.base;

public class ThreadSafeAsync<ItemT> implements ManagedAsync<ItemT> {
  private final SimpleAsync<ItemT> myAsync;

  public ThreadSafeAsync() {
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

  @Override
  public void success(ItemT item) {
    synchronized (myAsync) {
      myAsync.success(item);
    }
  }

  @Override
  public void failure(Throwable throwable) {
    synchronized (myAsync) {
      myAsync.failure(throwable);
    }
  }
}
