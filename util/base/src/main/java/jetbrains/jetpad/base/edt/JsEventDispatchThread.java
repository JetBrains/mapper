package jetbrains.jetpad.base.edt;

import com.google.gwt.core.client.Scheduler;

public final class JsEventDispatchThread implements EventDispatchThread {
  public static final JsEventDispatchThread INSTANCE = new JsEventDispatchThread();

  private JsEventDispatchThread() {
  }

  @Override
  public void schedule(final Runnable r) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        r.run();
      }
    });
  }
}
