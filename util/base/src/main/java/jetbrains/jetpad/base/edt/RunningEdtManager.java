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

import jetbrains.jetpad.base.Registration;

import java.util.ArrayList;
import java.util.List;

public class RunningEdtManager implements EdtManager, EventDispatchThread {
  private final String myName;
  private volatile boolean myFinished;
  private volatile boolean myExecuting = false;
  private volatile boolean myFlushing = false;
  private final List<Runnable> myTasks = new ArrayList<>();

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
  public void finish() {
    checkCanStop();
    final int tasksLeft = myTasks.size();
    if (!myFlushing) {
      flush(new Flusher() {
        @Override
        public int getLimit() {
          return tasksLeft;
        }
      });
    }
    shutdown();
  }

  @Override
  public void kill() {
    checkCanStop();
    myTasks.clear();
    shutdown();
  }

  protected void shutdown() {
    myFinished = true;
  }

  @Override
  public final void schedule(Runnable r) {
    if (checkCanSchedule() != null) {
      return;
    }
    doSchedule(r);
  }

  @Override
  public final Registration schedule(int delay, Runnable r) {
    Registration reg = checkCanSchedule();
    if (reg != null) {
      return reg;
    }
    return doSchedule(delay, r);
  }

  @Override
  public final Registration scheduleRepeating(int period, Runnable r) {
    Registration reg = checkCanSchedule();
    if (reg != null) {
      return reg;
    }
    return doScheduleRepeating(period, r);
  }

  Registration checkCanSchedule() {
    if (isStopped()) {
      throw new EdtException();
    }
    return null;
  }

  private void checkCanStop() {
    if (myFinished) {
      throw new IllegalStateException(RunningEdtManager.this + ": has already been stopped");
    }
  }

  @Override
  public boolean isStopped() {
    return myFinished;
  }

  final void flushAll() {
    flush(new Flusher() {
      @Override
      public int getLimit() {
        return myTasks.size();
      }
    });
  }

  final void flush(Flusher flusher) {
    if (myFlushing) {
      throw new IllegalStateException((RunningEdtManager.this + ": recursive flush is prohibited"));
    }
    myFlushing = true;
    int executedTasksCounter = 0;
    try {
      for (int i = 0; i < flusher.getLimit(); i++) {
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
        flushAll();
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

  interface Flusher {
    int getLimit();
  }
}
