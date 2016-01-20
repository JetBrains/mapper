package jetbrains.jetpad.mapper;

import jetbrains.jetpad.base.Registration;

public abstract class RegistrationSynchronizer implements Synchronizer {
  private Registration myReg;

  @Override
  public final void attach(SynchronizerContext ctx) {
    myReg = doAttach(ctx);
  }

  abstract Registration doAttach(SynchronizerContext ctx);

  @Override
  public final void detach() {
    myReg.remove();
  }
}
