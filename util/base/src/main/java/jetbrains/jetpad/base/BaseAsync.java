package jetbrains.jetpad.base;

public abstract class BaseAsync<ItemT> implements Async<ItemT> {
  @Override
  public final Async<ItemT> onSuccess(Handler<? super ItemT> successHandler) {
    handle(successHandler);
    return this;
  }

  @Override
  public final Async<ItemT> onFailure(Handler<Throwable> failureHandler) {
    handleFailure(failureHandler);
    return this;
  }
}
