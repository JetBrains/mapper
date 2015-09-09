package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Value;

public class EDT {
  public static Runnable validateInEdt(final EventDispatchThread validateOn, final Runnable validator) {
    final Value<Boolean> valid = new Value<>(true);

    final Runnable edtValidator = new Runnable() {
      @Override
      public void run() {
        if (valid.get()) return;
        validator.run();
        valid.set(true);
      }
    };

    return new Runnable() {
      @Override
      public void run() {
        valid.set(false);
        validateOn.schedule(edtValidator);
      }
    };
  }
}
