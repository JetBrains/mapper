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

import jetbrains.jetpad.base.Registration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class AwtEventDispatchThread implements EventDispatchThread {
  public static final AwtEventDispatchThread INSTANCE = new AwtEventDispatchThread();

  private AwtEventDispatchThread() {
  }

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }

  @Override
  public void schedule(Runnable r) {
    SwingUtilities.invokeLater(r);
  }

  @Override
  public Registration schedule(int delayMillis, final Runnable r) {
    final Timer timer = new Timer(delayMillis, null);
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
  public Registration scheduleRepeating(int periodMillis, final Runnable r) {
    final Timer timer = new Timer(periodMillis, null);
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