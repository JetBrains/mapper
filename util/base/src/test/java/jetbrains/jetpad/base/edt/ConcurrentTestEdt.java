/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Supplier;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentTestEdt implements EventDispatchThread, EdtManager {
  private final TestEventDispatchThread myEdt;
  private final Lock myLock = new ReentrantLock();

  public ConcurrentTestEdt() {
    this("");
  }

  public ConcurrentTestEdt(String name) {
    myEdt = new TestEventDispatchThread(name, new Runnable() {
      @Override
      public void run() {

      }
    });
  }

  @Override
  public long getCurrentTimeMillis() {
    myLock.lock();
    try {
      return myEdt.getCurrentTimeMillis();
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public Async<Void> schedule(Runnable r) throws EdtException {
    myLock.lock();
    try {
      return myEdt.schedule(r);
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public <ResultT> Async<ResultT> schedule(Supplier<ResultT> s) throws EdtException {
    myLock.lock();
    try {
      return myEdt.schedule(s);
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public Registration schedule(int delay, Runnable r) throws EdtException {
    myLock.lock();
    try {
      return myEdt.schedule(delay, r);
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) throws EdtException {
    myLock.lock();
    try {
      return myEdt.scheduleRepeating(period, r);
    } finally {
      myLock.unlock();
    }
  }

  public int executeUpdates() {
    myLock.lock();
    try {
      return myEdt.executeUpdates();
    } finally {
      myLock.unlock();
    }
  }

  public int executeUpdates(int passedTime) {
    myLock.lock();
    try {
      return myEdt.executeUpdates(passedTime);
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public EventDispatchThread getEdt() {
    return this;
  }

  @Override
  public void finish() {
    myLock.lock();
    try {
      myEdt.finish();
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public void kill() {
    myLock.lock();
    try {
      myEdt.kill();
    } finally {
      myLock.unlock();
    }
  }

  @Override
  public boolean isStopped() {
    myLock.lock();
    try {
      return myEdt.isFinished();
    } finally {
      myLock.unlock();
    }
  }
}
