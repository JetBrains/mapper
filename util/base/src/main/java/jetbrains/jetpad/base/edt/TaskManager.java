package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.edt.EventDispatchThread;

public interface TaskManager {
  EventDispatchThread getEDT();
  void finish();
  void kill();
  boolean isStopped();
}
