/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;

public class TestEventDispatchThreadTest {
  private TestEventDispatchThread edt = new TestEventDispatchThread();

  @Test
  public void simpleInvokeLater() {
    Runnable r = Mockito.mock(Runnable.class);
    edt.schedule(r);
    edt.executeUpdates();
    Mockito.verify(r).run();
  }

  @Test
  public void invokeLaterWithInvokeLater() {
    final Runnable r = Mockito.mock(Runnable.class);
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        edt.schedule(r);
      }
    });
    edt.executeUpdates();
    Mockito.verify(r).run();
  }

  @Test
  public void simpleInvokeLaterWithDelay() {
    Runnable r = Mockito.mock(Runnable.class);
    edt.schedule(100, r);
    edt.executeUpdates();
    Mockito.verifyZeroInteractions(r);
    edt.executeUpdates(100);
    Mockito.verify(r).run();
  }

  @Test
  public void simpleInvokeLaterRepeating() {
    Runnable r = Mockito.mock(Runnable.class);
    edt.scheduleRepeating(1, r);
    edt.executeUpdates(2);
    Mockito.verify(r, times(2)).run();
  }
}