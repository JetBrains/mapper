/*
 * Copyright 2012-2014 JetBrains s.r.o
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

public class TestEventDispatchThread implements EventDispatchThread {
  private int myCurrentTime;
  private int myModicationCount;
  private List<RunnableRecord> myRecords = new ArrayList<>();

  public void executeUpdates() {
    executeUpdates(0);
  }

  public void executeUpdates(int passedTime) {
    executeCurrentUpdates();
    for (int i = 0; i < passedTime; i++) {
      myCurrentTime++;
      executeCurrentUpdates();
    }
  }

  private void executeCurrentUpdates() {
    int mc;
    do {
      mc = myModicationCount;
      List<RunnableRecord> toRemove = new ArrayList<>();
      for (RunnableRecord r : new ArrayList<>(myRecords)) {
        if (r.getTargetTime() == myCurrentTime) {
          r.run();
          toRemove.add(r);
        }
      }
      myRecords.removeAll(toRemove);
    } while (myModicationCount != mc);
  }

  @Override
  public void schedule(Runnable r) {
    schedule(0, r);
  }

  @Override
  public Registration schedule(int delay, Runnable r) {
    myModicationCount++;
    final RunnableRecord record = new RunnableRecord(myCurrentTime + delay, r);
    myRecords.add(record);
    return new Registration() {
      @Override
      public void remove() {
        myRecords.remove(record);
      }
    };
  }

  @Override
  public Registration scheduleRepeating(final int period, final Runnable r) {
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
      public void remove() {
        cancelled.set(true);
      }
    };
  }

  private class RunnableRecord {
    private int myTargetTime;
    private Runnable myRunnable;

    private RunnableRecord(int targetTime, Runnable runnable) {
      myTargetTime = targetTime;
      myRunnable = runnable;
    }

    int getTargetTime() {
      return myTargetTime;
    }

    void run() {
      myRunnable.run();
    }
  }
}