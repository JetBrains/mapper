package jetbrains.jetpad.base.animation;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.EventDispatchThread;

public abstract class TimerAnimation implements Animation {
  private DefaultAnimation myDefaultAnimation = new DefaultAnimation() {
    @Override
    protected void doStop() {
    }
  };
  private Registration myTimerReg;
  private int myFrame;
  private int myMaxFrame;

  protected TimerAnimation(EventDispatchThread edt, int frames, int framePeriod) {
    if (frames <= 0) {
      throw new IllegalArgumentException();
    }

    myTimerReg = edt.scheduleRepeating(framePeriod, new Runnable() {
      @Override
      public void run() {
        onFrame();
      }
    });
    myMaxFrame = frames;
  }

  @Override
  public void stop() {
    myFrame = myMaxFrame;
    animateFrame(myFrame, true);
    lastFrame();
    myDefaultAnimation.stop();
  }

  private void onFrame() {
    myFrame++;
    animateFrame(myFrame, myFrame == myMaxFrame);
    if (myFrame == myMaxFrame) {
      lastFrame();
      myDefaultAnimation.done();
    }
  }

  private void lastFrame() {
    myTimerReg.remove();
  }

  protected abstract void animateFrame(int frame, boolean lastFrame);

  @Override
  public void whenDone(Runnable r) {
    myDefaultAnimation.whenDone(r);
  }
}
