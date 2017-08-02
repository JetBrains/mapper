/*
 * Copyright 2012-2016 JetBrains s.r.o
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
import jetbrains.jetpad.base.Value;

import java.util.ArrayList;
import java.util.List;

public final class TestEventDispatchThread implements EventDispatchThread {
  private final String myName;
  private final Runnable myThreadSafeChecker;

  private int myCurrentTime;
  private int myModificationCount;
  private List<RunnableRecord> myRecords = new ArrayList<>();
  private boolean myFinished = false;
  private boolean myRunning = false;

  private Thread myOwner;

  public TestEventDispatchThread() {
    this("");
  }

  public TestEventDispatchThread(String name) {
    resetOwner();
    myThreadSafeChecker = new Runnable() {
      @Override
      public void run() {
        Thread current = Thread.currentThread();
        if (myOwner != current) {
          throw new IllegalStateException("Test EDT is not thread-safe. Owner is " + myOwner + ". Attempt to access from " + current);
        }
      }
    };
    myName = name;
  }

  public TestEventDispatchThread(String name, Runnable threadSafeChecker) {
    myThreadSafeChecker = threadSafeChecker;
    myName = name;
  }

  public String getName() {
    return myName;
  }

  public boolean isEmpty() {
    return myRecords.isEmpty();
  }

  public int size() {
    return myRecords.size();
  }

  public void resetOwner() {
    myOwner = Thread.currentThread();
  }

  public boolean nothingScheduled(int time) {
    for (RunnableRecord record : myRecords) {
      if (record.myTargetTime <= myCurrentTime + time) {
        return false;
      }
    }
    return true;
  }

  public int executeUpdates() {
    return executeUpdates(0);
  }

  public int executeUpdates(int passedTime) {
    int runCommandsNum = 0;
    runCommandsNum += executeCurrentUpdates();
    for (int i = 0; i < passedTime; i++) {
      myCurrentTime++;
      runCommandsNum += executeCurrentUpdates();
    }
    return runCommandsNum;
  }

  private int executeCurrentUpdates() {
    myThreadSafeChecker.run();
    int runCommandsNum = 0;
    int mc;
    do {
      mc = myModificationCount;
      runCommandsNum += runCurrentRecords();
    } while (myModificationCount != mc);
    return runCommandsNum;
  }

  private int runCurrentRecords() {
    List<RunnableRecord> current = getCurrentRecords();
    int executedNum = 0;
    myRunning = true;
    try {
      for (RunnableRecord r : current) {
        executedNum++;
        r.myRunnable.run();
      }
    } finally {
      myRunning = false;
      myRecords.removeAll(current.subList(0, executedNum));
    }
    return executedNum;
  }

  private List<RunnableRecord> getCurrentRecords() {
    List<RunnableRecord> records = new ArrayList<>();
    for (RunnableRecord record : myRecords) {
      if (record.myTargetTime == myCurrentTime) {
        records.add(record);
      }
    }
    return records;
  }

  @Override
  public long getCurrentTimeMillis() {
    return myCurrentTime;
  }

  @Override
  public void schedule(Runnable r) {
    schedule(0, r);
  }

  @Override
  public Registration schedule(int delay, Runnable r) {
    myThreadSafeChecker.run();
    checkCanSchedule();
    myModificationCount++;
    final RunnableRecord record = new RunnableRecord(myCurrentTime + delay, r);
    myRecords.add(record);
    return new Registration() {
      @Override
      protected void doRemove() {
        myRecords.remove(record);
      }
    };
  }

  @Override
  public Registration scheduleRepeating(final int period, final Runnable r) {
    myThreadSafeChecker.run();
    checkCanSchedule();
    final Value<Boolean> cancelled = new Value<>(false);
    schedule(period, new Runnable() {
      @Override
      public void run() {
        if (cancelled.get()) return;
        r.run();
        schedule(period, this);
      }
    });
    return new Registration() {
      @Override
      protected void doRemove() {
        cancelled.set(true);
      }
    };
  }

  @Override
  public String toString() {
    return "TestEdt@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + myName + ")");
  }

  private void checkCanSchedule() {
    if (myFinished) {
      throw new EdtException();
    }
  }

  private void checkCanStop() {
    if (myFinished) {
      throw new IllegalStateException(this + " has been already finished");
    }
  }

  private void checkInsideTask() {
    if (myRunning) {
      throw new IllegalStateException(this + " is running a task");
    }
  }

  public void finish() {
    myThreadSafeChecker.run();
    checkCanStop();
    checkInsideTask();
    runCurrentRecords();
    shutdown();
  }

  public void kill() {
    myThreadSafeChecker.run();
    checkCanStop();
    checkInsideTask();
    shutdown();
  }

  private void shutdown() {
    myThreadSafeChecker.run();
    myRecords.clear();
    myFinished = true;
  }

  public boolean isFinished() {
    return myFinished;
  }

  private static class RunnableRecord {
    private final int myTargetTime;
    private final Runnable myRunnable;

    private RunnableRecord(int targetTime, Runnable runnable) {
      myTargetTime = targetTime;
      myRunnable = runnable;
    }
  }
}