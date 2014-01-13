package jetbrains.jetpad.base;

public interface EventDispatchThread {
  void schedule(Runnable r);
}
