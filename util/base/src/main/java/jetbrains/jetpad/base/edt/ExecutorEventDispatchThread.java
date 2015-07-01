package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorEventDispatchThread implements EventDispatchThread {
  private static final Logger LOG = Logger.getLogger(ExecutorEventDispatchThread.class.getName());

  private final ScheduledExecutorService myExecutor;
  private final Handler<Throwable> myErrorHandler;

  public ExecutorEventDispatchThread() {
    this(new Handler<Throwable>() {
      @Override
      public void handle(Throwable t) {
        LOG.log(Level.SEVERE, "Runnable submitted to ExecutorEventDispatchThread failed", t);
      }
    });
  }

  public ExecutorEventDispatchThread(Handler<Throwable> errorHandler) {
    myExecutor = Executors.newSingleThreadScheduledExecutor();
    myErrorHandler = errorHandler;
  }

  @Override
  public void schedule(Runnable r) {
    myExecutor.submit(handleFailure(r));
  }

  @Override
  public Registration schedule(int delay, final Runnable r) {
    return new FutureRegistration(myExecutor.schedule(handleFailure(r), delay, TimeUnit.MILLISECONDS));
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) {
    return new FutureRegistration(
        myExecutor.scheduleAtFixedRate(handleFailure(r), period, period, TimeUnit.MILLISECONDS));
  }

  private Runnable handleFailure(final Runnable r) {
    return new Runnable() {
      @Override
      public void run() {
        try {
          r.run();
        } catch (Throwable t) {
          myErrorHandler.handle(t);
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
