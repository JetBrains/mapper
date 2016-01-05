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

public final class ExecutorEdtManager implements EdtManager, EventDispatchThread {
  private static final int BIG_TIMEOUT_DAYS = 1;
  private final String myName;
  private final ExecutorEdt myEdt;

  public ExecutorEdtManager() {
    this("");
  }

  public ExecutorEdtManager(String name) {
    myName = name;
    myEdt = new ExecutorEdt(name);
  }

  @Override
  public EventDispatchThread getEdt() {
    return this;
  }

  @Override
  public void finish() {
    myEdt.getExecutor().shutdown();
    waitTermination();
  }

  @Override
  public void kill() {
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
      ThrowableHandlers.handle(new RuntimeException(ExecutorEdtManager.this
          + ": failed to finish ExecutorEdtManager in " + BIG_TIMEOUT_DAYS + " days"));
    }
  }

  @Override
  public boolean isStopped() {
    return isThreadInactive();
  }

  @Override
  public long getCurrentTimeMillis() {
    return myEdt.getCurrentTimeMillis();
  }

  @Override
  public void schedule(Runnable runnable) {
    try {
      myEdt.schedule(runnable);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
  }

  private boolean isThreadInactive() {
    return myEdt.getExecutor().isShutdown();
  }

  @Override
  public Registration schedule(int delayMillis, Runnable r) {
    Registration reg;
    try {
      reg = myEdt.schedule(delayMillis, r);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
    return reg;
  }

  @Override
  public Registration scheduleRepeating(int periodMillis, Runnable r) {
    Registration reg;
    try {
      reg = myEdt.scheduleRepeating(periodMillis, r);
    } catch (RejectedExecutionException e) {
      throw new EdtException(e);
    }
    return reg;
  }

  @Override
  public String toString() {
    return "ExecutorEdtManager@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + myName + ")");
  }

  private class ExecutorEdt implements EventDispatchThread {
    private final ScheduledExecutorService myExecutor;
    private volatile Handler<Throwable> myErrorHandler = null;

    ExecutorEdt(String name) {
      myExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name));
    }

    @Override
    public long getCurrentTimeMillis() {
      return System.currentTimeMillis();
    }

    @Override
    public void schedule(Runnable r) {
      myExecutor.submit(handleFailure(r));
    }

    @Override
    public Registration schedule(int delayMillis, Runnable r) {
      return new FutureRegistration(myExecutor.schedule(handleFailure(r), delayMillis, TimeUnit.MILLISECONDS));
    }

    @Override
    public Registration scheduleRepeating(int periodMillis, Runnable r) {
      return new FutureRegistration(
          myExecutor.scheduleAtFixedRate(handleFailure(r), periodMillis, periodMillis, TimeUnit.MILLISECONDS));
    }

    private Runnable handleFailure(final Runnable r) {
      if (myErrorHandler == null) {
        myErrorHandler = new Handler<Throwable>() {
          @Override
          public void handle(Throwable t) {
            ThrowableHandlers.handle(new RuntimeException(ExecutorEdt.this + ": exception", t));
          }
        };
      }
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

    // do not use it directly, only for state checks and finalization.
    ExecutorService getExecutor() {
      return myExecutor;
    }

    @Override
    public String toString() {
      return "ExecutorEdt@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + myName + ')');
    }
  }

  private static class FutureRegistration extends Registration {
    private final Future<?> myFuture;

    private FutureRegistration(Future<?> future) {
      myFuture = future;
    }

    @Override
    protected void doRemove() {
      myFuture.cancel(false);
    }
  }

  private static class NamedThreadFactory implements ThreadFactory {
    private final String myName;

    NamedThreadFactory(String name) {
      myName = name;
    }

    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, myName);
    }
  }
}
