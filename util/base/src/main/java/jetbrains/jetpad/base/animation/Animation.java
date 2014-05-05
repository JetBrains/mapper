package jetbrains.jetpad.base.animation;

public interface Animation {
  void stop();

  void whenDone(Runnable r);
}
