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

import org.junit.Test;
import org.mockito.Mockito;

public class TestEdtManagerTest {
  private TestEdtManager manager = new TestEdtManager();
  private Runnable r = Mockito.mock(Runnable.class);

  @Test
  public void finish() {
    manager.getEdt().schedule(r);
    manager.finish();
    Mockito.verify(r).run();
  }

  @Test
  public void kill() {
    manager.getEdt().schedule(r);
    manager.kill();
    Mockito.verifyZeroInteractions(r);
  }

  @Test
  public void scheduleDuringWaitForFinish() {
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        manager.getEdt().schedule(r);
      }
    });
    manager.finish();
    Mockito.verifyZeroInteractions(r);
  }

  @Test(expected = EdtException.class)
  public void scheduleAfterFinish() {
    manager.finish();
    manager.getEdt().schedule(r);
  }

  @Test(expected = EdtException.class)
  public void delayedScheduleAfterFinish() {
    manager.finish();
    manager.getEdt().schedule(10, new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  @Test(expected = EdtException.class)
  public void scheduleRepeatingAfterFinish() {
    manager.finish();
    manager.getEdt().schedule(r);
  }

  @Test(expected = IllegalStateException.class)
  public void killFromTask() {
    manager.getEdt().schedule(() -> manager.kill());
    manager.getEdt().executeUpdates();
  }

  @Test(expected = IllegalStateException.class)
  public void finishFromTask() {
    manager.getEdt().schedule(() -> manager.finish());
    manager.getEdt().executeUpdates();
  }
}