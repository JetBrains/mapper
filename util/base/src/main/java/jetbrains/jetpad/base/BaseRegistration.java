package jetbrains.jetpad.base;

public abstract class BaseRegistration implements Registration {
  private boolean myRemoved;

  protected abstract void doRemove();

  @Override
  public final void remove() {
    if (myRemoved) {
      throw new IllegalStateException();
    }
    doRemove();
    myRemoved = true;
  }


}
