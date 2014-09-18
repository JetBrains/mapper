package jetbrains.jetpad.base;

public class ThreadSafeSimpleAsync<ItemT> implements Async<ItemT> {
  private final SimpleAsync<ItemT> myAsync;

  public ThreadSafeSimpleAsync() {
    myAsync = new SimpleAsync<>();
  }

  @Override
  public Async<ItemT> onSuccess(final Handler<? super ItemT> handler) {
    synchronized (myAsync) {
      myAsync.onSuccess(handler);
      return this;
    }
  }

  @Override
  public Async<ItemT> onFailure(final Handler<Throwable> handler) {
    synchronized (myAsync) {
      myAsync.onFailure(handler);
      return this;
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
