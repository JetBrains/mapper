/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.base.edt;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;

public final class JsEventDispatchThread implements EventDispatchThread {
  public static final JsEventDispatchThread INSTANCE = new JsEventDispatchThread();

  private JsEventDispatchThread() {
  }

  @Override
  public void schedule(final Runnable r) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        r.run();
      }
    });
  }

  @Override
  public Registration schedule(int delay, final Runnable r) {
    final Timer timer = new Timer() {
      @Override
      public void run() {
        r.run();
      }
    };
    timer.schedule(delay);
    return timerReg(timer);
  }

  @Override
  public Registration scheduleRepeating(int period, final Runnable r) {
    final Timer timer = new Timer() {
      @Override
      public void run() {
        r.run();
      }
    };
    timer.scheduleRepeating(period);
    return timerReg(timer);
  }

  private Registration timerReg(final Timer timer) {
    return new Registration() {
      @Override
      public void remove() {
        if (timer.isRunning()) {
          timer.cancel();
        }
      }
    };
  }
}