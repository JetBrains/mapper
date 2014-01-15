package jetbrains.jetpad.base.edt;

import java.util.ArrayList;
import java.util.List;

public class TestEventDispatchThread implements EventDispatchThread {
  private List<Runnable> myRunnables = new ArrayList<Runnable>();

  public void executeUpdates() {
    while (!myRunnables.isEmpty()) {
      List<Runnable> toExecute = new ArrayList<Runnable>(myRunnables);
      myRunnables.clear();
      for (Runnable runnable : toExecute) {
        runnable.run();
      }
    }
  }

  @Override
  public void schedule(Runnable r) {
    myRunnables.add(r);
  }
}
