package jetbrains.jetpad.base;

public interface Async<ItemT> {
  Async<ItemT> onSuccess(Handler<? super ItemT> successHandler);
  Async<ItemT> onFailure(Handler<Throwable> failureHandler);
}
