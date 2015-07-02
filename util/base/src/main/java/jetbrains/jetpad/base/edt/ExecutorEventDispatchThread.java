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

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorEventDispatchThread implements EventDispatchThread {
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
  public ExecutorService getExecutor() {
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
