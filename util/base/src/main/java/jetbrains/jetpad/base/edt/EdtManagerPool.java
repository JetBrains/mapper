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

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public final class EdtManagerPool implements EdtManagerFactory {
  private static final Logger LOG = Logger.getLogger(EdtManagerPool.class.getName());

  private final String myName;
  private final Object myLock;
  private final EdtManagerFactory myFactory;
  private final int myPoolSize;
  private final EdtManager[] myManagers;
  private final int[] myWorkingAdapters;
  private int myCurManager;
  private int myCreatedManagersNum;

  public EdtManagerPool(String name, int poolSize, EdtManagerFactory factory) {
    myLock = new Object();
    myName = name;
    myPoolSize = poolSize;
    myFactory = factory;
    myManagers = new EdtManager[myPoolSize];
    myWorkingAdapters = new int[myPoolSize];
    myCurManager = 0;
    myCreatedManagersNum = 0;
    LOG.fine("Created EDT managers pool, name=" + name + ", size=" + poolSize);
  }

  @Override
  public EdtManager createEdtManager(String name) {
    synchronized (myLock) {
      int cur = getCurManagerIndex();
      incWorkingAdapters(cur);
      EdtManagerAdapter adapter = new EdtManagerAdapter(name, myManagers[cur], cur);
      LOG.fine("Created adapter " + adapter + " for " + myManagers[cur] + ", index=" + cur);
      return adapter;
    }
  }

  private int getCurManagerIndex() {
    int cur = myCurManager++;
    if (myCurManager == myPoolSize) {
      myCurManager = 0;
    }
    return cur;
  }

  private void incWorkingAdapters(int index) {
    if (myWorkingAdapters[index] == 0) {
      if (myManagers[index] != null) {
        throw new IllegalStateException();
      }
      myManagers[index] = myFactory.createEdtManager(myName + "_" + index + "_" + myCreatedManagersNum++);
      LOG.fine("Pool " + myName + " created " + myManagers[index] + " at index " + index);
    }
    myWorkingAdapters[index]++;
  }

  private void decWorkingAdapters(int index) {
    synchronized (myLock) {
      myWorkingAdapters[index]--;
      if (myWorkingAdapters[index] == 0) {
        LOG.fine("Pool " + myName + " shut down " + myManagers[index] + " at index " + index);
        myManagers[index].kill();
        myManagers[index] = null;
      }
    }
  }

  //for test
  boolean isEmpty() {
    synchronized (myLock) {
      for (EdtManager manager : myManagers) {
        if (manager != null) {
          return false;
        }
      }
      return true;
    }
  }

  //for test
  boolean checkManager(EdtManager manager) {
    synchronized (myLock) {
      EdtManagerAdapter adapter = (EdtManagerAdapter) manager;
      return adapter.myManager == myManagers[adapter.myIndex];
    }
  }

  private class EdtManagerAdapter implements EdtManager, EventDispatchThread {
    private final String myName;
    //we can't use here only myIndex because myManagers[] is guarded by a lock
    private final EdtManager myManager;
    private final int myIndex;

    private EdtManagerAdapter(String name, EdtManager manager, int index) {
      myName = name;
      myManager = manager;
      myIndex = index;
    }

    @Override
    public EventDispatchThread getEdt() {
      return this;
    }

    @Override
    public void finish() {
      final CountDownLatch latch = new CountDownLatch(1);
      myManager.getEdt().schedule(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
          }
        });
      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        decWorkingAdapters(myIndex);
      }
    }

    @Override
    public void kill() {
      decWorkingAdapters(myIndex);
    }

    @Override
    public boolean isStopped() {
      return myManager.isStopped();
    }

    @Override
    public long getCurrentTimeMillis() {
      return myManager.getEdt().getCurrentTimeMillis();
    }

    @Override
    public Async<Void> schedule(Runnable runnable) {
      return myManager.getEdt().schedule(runnable);
    }

    @Override
    public <ResultT> Async<ResultT> schedule(Supplier<ResultT> s) {
      return myManager.getEdt().schedule(s);
    }

    @Override
    public <ResultT> Async<ResultT> flatSchedule(Supplier<Async<ResultT>> s) {
      return myManager.getEdt().flatSchedule(s);
    }

    @Override
    public Registration schedule(int delay, Runnable runnable) {
      return myManager.getEdt().schedule(delay, runnable);
    }

    @Override
    public Registration scheduleRepeating(int period, Runnable runnable) {
      return myManager.getEdt().scheduleRepeating(period, runnable);
    }

    @Override
    public String toString() {
      return "EdtManagerPool.EdtManagerAdapter@" + Integer.toHexString(hashCode())
          + ("".equals(myName) ? "" : " (" + myName + ")");
    }
  }
}
