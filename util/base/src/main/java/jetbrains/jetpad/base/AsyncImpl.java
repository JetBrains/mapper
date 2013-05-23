package jetbrains.jetpad.base;

import java.util.ArrayList;
import java.util.List;

public class AsyncImpl<ItemT> implements Async<ItemT> {
  private ItemT mySuccessItem = null;
  private boolean mySucceeded = false;

  private Throwable myFailureThrowable = null;
  private boolean myFailed = false;

  private List<Handler<ItemT>> mySuccessHandlers = new ArrayList<Handler<ItemT>>();
  private List<Handler<Throwable>> myFailureHandlers = new ArrayList<Handler<Throwable>>();

  public Async<ItemT> onSuccess(Handler<ItemT> handler) {
    if (mySucceeded) {
      handler.handle(mySuccessItem);
    } else {
      mySuccessHandlers.add(handler);
    }
    return this;
  }

  public Async<ItemT> onFailure(Handler<Throwable> handler) {
    if (myFailed) {
      handler.handle(myFailureThrowable);
    } else {
      myFailureHandlers.add(handler);
    }
    return this;
  }

  public void success(ItemT item) {
    if (alreadyHandled()) throw new IllegalStateException();

    for (Handler<ItemT> handler: mySuccessHandlers) {
      handler.handle(item);
    }
    clearHandlers();
    mySuccessItem = item;
    mySucceeded = true;
  }

  public void failure(Throwable throwable) {
    if (alreadyHandled()) throw new IllegalStateException();

    for (Handler<Throwable> handler: myFailureHandlers) {
      handler.handle(throwable);
    }
    clearHandlers();
    myFailureThrowable = throwable;
    myFailed = true;
  }

  private void clearHandlers() {
    mySuccessHandlers.clear();
    myFailureHandlers.clear();
  }

  private boolean alreadyHandled() {
    return mySucceeded || myFailed;
  }
}

