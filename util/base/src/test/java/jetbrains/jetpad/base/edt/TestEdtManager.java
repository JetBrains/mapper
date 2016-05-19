package jetbrains.jetpad.base.edt;

public class TestEdtManager implements EdtManager {
  private TestEventDispatchThread myEdt;

  public TestEdtManager() {
    this("");
  }

  public TestEdtManager(String name) {
    myEdt = new TestEventDispatchThread(name);
  }

  @Override
  public TestEventDispatchThread getEdt() {
    return myEdt;
  }

  @Override
  public void finish() {
    myEdt.finish();
  }

  @Override
  public void kill() {
    myEdt.kill();
  }

  @Override
  public boolean isStopped() {
    return myEdt.isFinished();
  }

  @Override
  public String toString() {
    return "manager for " + myEdt;
  }
}
