package jetbrains.jetpad.base.edt;

public class BufferingTaskManager extends RunningTaskManager {
  public BufferingTaskManager() {
    super();
  }

  public BufferingTaskManager(String name) {
    super(name);
  }

  @Override
  public void doSchedule(Runnable r) {
    addTaskToQueue(r);
  }

  public void flush() {
    flushAll();
  }

  public void flush(final int number) {
    flush(new Flusher() {
      @Override
      public int getLimit() {
        return number;
      }
    });
  }
}
