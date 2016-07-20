package jetbrains.jetpad.base;

/**
 * Allows only one task to be executed in the same time.
 *
 * Designed to prevent recursion in callbacks.
 */
public class ExclusiveExecutor {
  private boolean myRunning = false;

  public void execute(Runnable task) {
    if (!myRunning) {
      myRunning = true;
      try {
        task.run();
      } finally {
        myRunning = false;
      }
    }
  }
}
