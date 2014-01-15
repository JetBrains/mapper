package jetbrains.jetpad.base.edt;

import javax.swing.*;

public final class AwtEventDispatchThread implements EventDispatchThread {
  public static final AwtEventDispatchThread INSTANCE = new AwtEventDispatchThread();

  private AwtEventDispatchThread() {
  }

  @Override
  public void schedule(Runnable r) {
    SwingUtilities.invokeLater(r);
  }
}
