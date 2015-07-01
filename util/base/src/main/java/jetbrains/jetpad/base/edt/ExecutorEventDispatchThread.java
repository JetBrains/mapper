package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorEventDispatchThread implements EventDispatchThread {
  private static final Logger LOG = Logger.getLogger(ExecutorEventDispatchThread.class.getName());

  private final ScheduledExecutorService myExecutor;

  public ExecutorEventDispatchThread() {
    myExecutor = Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void schedule(Runnable r) {
    myExecutor.submit(logFailure(r));
  }

  @Override
  public Registration schedule(int delay, final Runnable r) {
    return new FutureRegistration(myExecutor.schedule(logFailure(r), delay, TimeUnit.MILLISECONDS));
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) {
    return new FutureRegistration(
        myExecutor.scheduleAtFixedRate(logFailure(r), period, period, TimeUnit.MILLISECONDS));
  }

  private static Runnable logFailure(final Runnable r) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          r.run();
        } catch (Throwable t) {
          LOG.log(Level.SEVERE, "Runnable submitted to ExecutorEventDispatchThread failed", t);
        }
      }
    };
  }

  /**
   * Do not use it directly, only for state checks and finalization.
   */
  public ExecutorService getExecutor() {
    return myExecutor;
  }

  private static class FutureRegistration extends Registration {
    private final Future<?> future;

    private FutureRegistration(Future<?> future) {
      this.future = future;
    }

    @Override
    protected void doRemove() {
      future.cancel(false);
    }
  }
}
