package jetbrains.jetpad.base.animation;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.SimpleAsync;

public abstract class DefaultAnimation implements Animation {
  private SimpleAsync<Object> myWhenDone = new SimpleAsync<>();
  private boolean myDone;

  protected abstract void doStop();

  public void done() {
    if (myDone) {
      throw new IllegalStateException();
    }
    myWhenDone.success(null);
    myDone = true;
  }

  @Override
  public void stop() {
    if (myDone) {
      throw new IllegalStateException();
    }
    doStop();
  }

  @Override
  public void whenDone(final Runnable r) {
    myWhenDone.onSuccess(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        r.run();
      }
    });
  }
}
