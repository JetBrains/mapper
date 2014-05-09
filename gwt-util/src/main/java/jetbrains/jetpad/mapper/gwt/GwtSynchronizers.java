package jetbrains.jetpad.mapper.gwt;

import com.google.common.base.Supplier;
import com.google.gwt.event.shared.HandlerRegistration;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.SynchronizerContext;

public class GwtSynchronizers {
  public static Synchronizer forRegistration(final Supplier<HandlerRegistration> reg) {
    return new Synchronizer() {
      HandlerRegistration myReg;

      @Override
      public void attach(SynchronizerContext ctx) {
        myReg = reg.get();
      }

      @Override
      public void detach() {
        myReg.removeHandler();
      }
    };
  }

  public static Synchronizer forRegistration(final HandlerRegistration r) {
    return new Synchronizer() {
      @Override
      public void attach(SynchronizerContext ctx) {
      }

      @Override
      public void detach() {
        r.removeHandler();
      }
    };
  }
}
