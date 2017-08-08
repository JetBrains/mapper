package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;

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
  public void schedule(Runnable r) throws EdtException {
    myLock.lock();
    try {
      myEdt.schedule(r);
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

  public boolean isEmpty() {
    myLock.lock();
    try {
      return myEdt.isEmpty();
    } finally {
      myLock.unlock();
    }
  }
}
