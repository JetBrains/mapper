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

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;

import java.util.ArrayList;
import java.util.List;

public class RunningEdtManager implements EdtManager, EventDispatchThread {
  private String myName;
  private List<Runnable> myTasks = new ArrayList<>();
  private boolean myFinished = false;
  private boolean myExecuting = false;
  private boolean myFlushing = false;

  public RunningEdtManager() {
    this("");
  }

  public RunningEdtManager(String name) {
    myName = name;
  }

  @Override
  public EventDispatchThread getEdt() {
    return this;
  }

  @Override
  public final void finish() {
    checkCanStop();
    if (!myFlushing) {
      flush(myTasks.size());
    }
    shutdown();
  }

  @Override
  public final void kill() {
    checkCanStop();
    myTasks.clear();
    shutdown();
  }

  protected void shutdown() {
    myFinished = true;
  }

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  public final void schedule(Runnable r) {
    checkCanSchedule();
    doSchedule(r);
  }

  @Override
  public final Registration schedule(int delay, Runnable r) {
    checkCanSchedule();
    return doSchedule(delay, r);
  }

  @Override
  public final Registration scheduleRepeating(int period, Runnable r) {
    checkCanSchedule();
    return doScheduleRepeating(period, r);
  }

  private void checkCanSchedule() {
    if (isStopped()) {
      throw new EdtException();
    }
  }

  private void checkCanStop() {
    if (myFinished) {
      throw new IllegalStateException(RunningEdtManager.this + ": has already been stopped");
    }
  }

  @Override
  public final boolean isStopped() {
    return myFinished;
  }

  public final void flush() {
    flush(new Supplier<Integer>() {
      @Override
      public Integer get() {
        return myTasks.size();
      }
    });
  }

  public final void flush(final int tasksNum) {
    flush(new Supplier<Integer>() {
      @Override
      public Integer get() {
        return tasksNum;
      }
    });
  }

  private void flush(Supplier<Integer> tasksCount) {
    if (myFlushing) {
      throw new IllegalStateException((RunningEdtManager.this + ": recursive flush is prohibited"));
    }
    myFlushing = true;
    int executedTasksCounter = 0;
    try {
      for (int i = 0; i < tasksCount.get(); i++) {
        executedTasksCounter++;
        executeTask(myTasks.get(i));
      }
    } finally {
      myFlushing = false;
      if (!isStopped()) {
        myTasks.subList(0, executedTasksCounter).clear();
      }
    }
  }

  protected void doExecuteTask(Runnable r) {
    r.run();
  }

  private void executeTask(Runnable r) {
    myExecuting = true;
    try {
      doExecuteTask(r);
    } catch (Throwable t) {
      ThrowableHandlers.handle(t);
    } finally {
      myExecuting = false;
    }
  }

  public int size() {
    return myTasks.size();
  }

  public boolean isEmpty() {
    return myTasks.isEmpty();
  }

  void addTaskToQueue(Runnable r) {
    myTasks.add(r);
  }

  protected void doSchedule(Runnable r) {
    if (myExecuting) {
      myTasks.add(r);
    } else {
      if (!myTasks.isEmpty()) {
        throw new IllegalStateException();
      }
      executeTask(r);
      if (!isStopped()) {
        flush();
      }
    }
  }

  protected Registration doSchedule(int delay, Runnable r) {
    throw new UnsupportedOperationException();
  }

  protected Registration doScheduleRepeating(int period, Runnable r) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "RunningEdtManager@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + myName + ")");
  }
}
