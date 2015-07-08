package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;

public abstract class BaseTaskManager implements TaskManager, EventDispatchThread {
  private volatile boolean myFinished;
  private final String myName;

  public BaseTaskManager(String name) {
    myName = name;
  }

  @Override
  public final EventDispatchThread getEDT() {
    return this;
  }

  @Override
  public void finish() {
    checkCanStop();
  }

  @Override
  public void kill() {
    checkCanStop();
  }

  @Override
  public boolean isStopped() {
    return myFinished;
  }

  public String getName() {
    return myName;
  }

  protected void shutdown() {
    myFinished = true;
  }

  private void checkCanStop() {
    if (myFinished) {
      throw new IllegalStateException(wrapMessage("has already been stopped"));
    }
  }

  @Override
  public final void schedule(Runnable r) {
    if (isStopped()) {
      return;
    }
    doSchedule(r);
  }

  protected abstract void doSchedule(Runnable runnable);

  @Override
  public final Registration schedule(int delay, Runnable r) {
    if (isStopped()) {
      return Registration.EMPTY;
    }
    return doSchedule(delay, r);
  }

  protected abstract Registration doSchedule(int delay, Runnable runnable);

  @Override
  public final Registration scheduleRepeating(int period, Runnable r) {
    if (isStopped()) {
      return Registration.EMPTY;
    }
    return doScheduleRepeating(period, r);
  }

  protected abstract Registration doScheduleRepeating(int period, Runnable runnable);

  protected String wrapMessage(String message) {
    return this + ": " + message;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    int dotIndex = name.lastIndexOf('.');
    String className = dotIndex == 1 ? name : name.substring(dotIndex + 1);
    return className + "@" + Integer.toHexString(hashCode()) + ("".equals(myName) ? "" : " (" + getName() + ")");
  }

}
