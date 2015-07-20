/*
 * Copyright 2012-2015 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExecutorEdtManager extends BaseEdtManager {
  private static final int BIG_TIMEOUT_DAYS = 1;
  private final ExecutorEventDispatchThread myEdt;
  private volatile boolean myInitialized;

  public ExecutorEdtManager() {
    this("");
  }

  public ExecutorEdtManager(String name) {
    super(name);
    myEdt = new ExecutorEventDispatchThread(name);
  }

  private EventDispatchThread getMyEdt() {
    lazyInit();
    return myEdt;
  }

  private void lazyInit() {
    if (!myInitialized) {
      myInitialized = true;
      myEdt.setErrorHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable t) {
          handleException(new RuntimeException(wrapMessage("exception"), t));
        }
      });
    }
  }

  @Override
  public void finish() {
    super.finish();
    myEdt.getExecutor().shutdown();
    shutdown();
    waitTermination();
  }

  @Override
  public void kill() {
    super.kill();
    myEdt.getExecutor().shutdownNow();
    waitTermination();
  }

  private void waitTermination() {
    boolean terminated = false;
    try {
      terminated = myEdt.getExecutor().awaitTermination(BIG_TIMEOUT_DAYS, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (!terminated) {
      handleException(new RuntimeException("failed to finish ThreadyTaskManager in " + BIG_TIMEOUT_DAYS +
          " days"));
    }
  }

  @Override
  public boolean isStopped() {
    return super.isStopped() && isThreadInactive();
  }

  @Override
  protected void doSchedule(Runnable runnable) {
    getMyEdt().schedule(runnable);
  }


  private boolean isThreadInactive() {
    return myEdt.getExecutor().isShutdown();
  }

  protected void handleException(RuntimeException e) {
    ThrowableHandlers.handle(e);
  }

  @Override
  public Registration doSchedule(int delay, Runnable r) {
    return getMyEdt().schedule(delay, r);
  }

  @Override
  public Registration doScheduleRepeating(int period, Runnable r) {
    return getMyEdt().scheduleRepeating(period, r);
  }

  @Override
  protected void doScheduleAndWaitCompletion(final Runnable r) {
    final CountDownLatch latch = new CountDownLatch(1);
    doSchedule(new Runnable() {
      @Override
      public void run() {
        try {
          r.run();
        } finally {
          latch.countDown();
        }
      }
    });
    boolean ok = false;
    try {
      ok = latch.await(BIG_TIMEOUT_DAYS, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      handleException(new RuntimeException(e));
    }
    if (!ok) {
      handleException(new IllegalStateException("Failed to complete a task in " + BIG_TIMEOUT_DAYS + " day(s)"));
    }
  }

  private static class ExecutorEventDispatchThread implements EventDispatchThread {
    private static final Logger LOG = Logger.getLogger(ExecutorEventDispatchThread.class.getName());

    private final ScheduledExecutorService myExecutor;
    private volatile Handler<Throwable> myErrorHandler;

    public ExecutorEventDispatchThread() {
      myExecutor = Executors.newSingleThreadScheduledExecutor();
      setErrorHandler();
    }

    public ExecutorEventDispatchThread(String name) {
      myExecutor = Executors.newSingleThreadScheduledExecutor(new OurNamedThreadFactory(name));
      setErrorHandler();
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
    ExecutorService getExecutor() {
      return myExecutor;
    }

    public void setErrorHandler(Handler<Throwable> errorHandler) {
      myErrorHandler = errorHandler;
    }

    public void setErrorHandler() {
      setErrorHandler(new Handler<Throwable>() {
        @Override
        public void handle(Throwable t) {
          LOG.log(Level.SEVERE, "Runnable submitted to ExecutorEventDispatchThread failed", t);
        }
      });
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

    private static class OurNamedThreadFactory implements ThreadFactory {
      private final String myName;

      OurNamedThreadFactory(String name) {
        myName = name;
      }

      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, myName);
      }
    }
  }
}
