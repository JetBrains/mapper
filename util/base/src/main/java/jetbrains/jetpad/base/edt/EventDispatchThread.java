package jetbrains.jetpad.base.edt;

public interface EventDispatchThread {
  void schedule(Runnable r);
}
