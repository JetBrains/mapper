/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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