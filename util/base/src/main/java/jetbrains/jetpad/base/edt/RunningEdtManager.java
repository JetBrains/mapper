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

public class RunningEdtManager extends BaseEdtManager {
  private volatile boolean myExecuting = false;
  private volatile boolean myFlushing = false;
  private final List<Runnable> myTasks = new ArrayList<>();

  public RunningEdtManager() {
    this("");
  }

  public RunningEdtManager(String name) {
    super(name);
  }

  @Override
  public void finish() {
    super.finish();
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
    super.kill();
    myTasks.clear();
    shutdown();
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
      throw new IllegalStateException(wrapMessage("recursive flush is prohibited"));
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

  @Override
  public void doSchedule(Runnable r) {
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

  @Override
  public Registration doSchedule(int delay, Runnable r) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Registration doScheduleRepeating(int period, Runnable r) {
    throw new UnsupportedOperationException();
  }

  interface Flusher {
    int getLimit();
  }
}
