package jetbrains.jetpad.base;

public class InstantAsync<ValueT> implements Async<ValueT> {
  public final ValueT value;

  public InstantAsync(ValueT value) {
    this.value = value;
  }

  public Registration onSuccess(Handler<? super ValueT> successHandler) {
    successHandler.handle(value);
    return Registration.EMPTY;
  }

  public Registration onResult(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
    return onSuccess(successHandler);
  }

  public Registration onFailure(Handler<Throwable> failureHandler) {
    return Registration.EMPTY;
  }
}