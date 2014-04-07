package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;

public class NullEventDispatchThread implements EventDispatchThread {
  @Override
  public void schedule(Runnable r) {
  }

  @Override
  public Registration schedule(int delay, Runnable r) {
    return Registration.EMPTY;
  }

  @Override
  public Registration scheduleRepeating(int period, Runnable r) {
    return Registration.EMPTY;
  }
}
