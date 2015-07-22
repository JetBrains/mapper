package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Handler;

public class ValueListeners<ValueT> extends Listeners<Handler<ValueT>> {
  public void fire(final ValueT value) {
    fire(new ListenerCaller<Handler<ValueT>>() {
      @Override
      public void call(Handler<ValueT> l) {
        l.handle(value);
      }
    });
  }
}
