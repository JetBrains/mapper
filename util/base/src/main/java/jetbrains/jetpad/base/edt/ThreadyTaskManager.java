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

import java.util.concurrent.TimeUnit;


public class ThreadyTaskManager extends BaseTaskManager {
  private static final int BIG_TIMEOUT_DAYS = 1;
  private final ExecutorEventDispatchThread myEdt;
  private volatile boolean myInitialized;

  public ThreadyTaskManager(String name) {
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
}
