package jetbrains.jetpad.base;

public class FailureAsync<ValueT> implements Async<ValueT> {
  public final Throwable throwable;

  public FailureAsync(Throwable throwable) {
    this.throwable = throwable;
  }

  public Registration onSuccess(Handler<? super ValueT> successHandler) {
    return Registration.EMPTY;
  }

  public Registration onResult(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
    return onFailure(failureHandler);
  }

  public Registration onFailure(Handler<Throwable> failureHandler) {
    failureHandler.handle(throwable);
    return Registration.EMPTY;
  }
}
