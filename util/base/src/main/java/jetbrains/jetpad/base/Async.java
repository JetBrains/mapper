package jetbrains.jetpad.base;

public interface Async<ItemT> {
  Async<ItemT> onSuccess(Handler<ItemT> successHandler);
  Async<ItemT> onFailure(Handler<Throwable> failureHandler);
}
