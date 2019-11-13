/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Registration;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class AwtEventDispatchThread extends DefaultAsyncEdt {
  public static final AwtEventDispatchThread INSTANCE = new AwtEventDispatchThread();

  private AwtEventDispatchThread() {
  }

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  protected <ResultT> Async<ResultT> asyncSchedule(RunnableWithAsync<ResultT> runnableWithAsync) {
    SwingUtilities.invokeLater(runnableWithAsync);
    return runnableWithAsync;
  }

  @Override
  public Registration schedule(int delay, final Runnable r) {
    Timer timer = new Timer(delay, null);
    timer.setRepeats(false);
    timer.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        r.run();
      }
    });
    timer.start();
    return timerReg(timer);
  }

  @Override
  public Registration scheduleRepeating(int period, final Runnable r) {
    Timer timer = new Timer(period, null);
    timer.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        r.run();
      }
    });
    timer.start();
    return timerReg(timer);
  }

  private Registration timerReg(final Timer timer) {
    return new Registration() {
      @Override
      protected void doRemove() {
        if (timer.isRunning()) {
          timer.stop();
        }
      }
    };
  }
}
