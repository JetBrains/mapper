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
package jetbrains.jetpad.base.edt;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;

import java.util.logging.Logger;

public final class JsEventDispatchThread implements EventDispatchThread {
  private static final Logger LOG = Logger.getLogger(JsEventDispatchThread.class.getName());

  public static final JsEventDispatchThread INSTANCE = new JsEventDispatchThread();

  private JsEventDispatchThread() {
  }

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  public void schedule(final Runnable r) {
    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        doExecute(r);
      }
    });
  }

  @Override
  public Registration schedule(int delay, final Runnable r) {
    Timer timer = new Timer() {
      @Override
      public void run() {
        doExecute(r);
      }
    };
    timer.schedule(delay);
    return timerReg(timer);
  }

  @Override
  public Registration scheduleRepeating(final int period, final Runnable r) {
    Timer timer = new Timer() {
      private long myLastInvocation = 0L;
      @Override
      public void run() {
        long current = getCurrentTimeMillis();
        if (current - myLastInvocation < period) return;
        myLastInvocation = current;
        doExecute(r);
      }
    };
    timer.scheduleRepeating(period);
    return timerReg(timer);
  }

  private void doExecute(Runnable r) {
    try {
      r.run();
    } catch (JavaScriptException jse) {
      if (jse.isThrownSet()) {
        LOG.severe("Caught JavaScriptException, wrapped error is: " + jse.getThrown());
      }
      ThrowableHandlers.handle(jse);
    } catch (Throwable t) {
      ThrowableHandlers.handle(t);
    }
  }

  private Registration timerReg(final Timer timer) {
    return new Registration() {
      @Override
      protected void doRemove() {
        if (timer.isRunning()) {
          timer.cancel();
        }
      }
    };
  }
}