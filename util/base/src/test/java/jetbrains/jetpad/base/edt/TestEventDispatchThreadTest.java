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

import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

public class TestEventDispatchThreadTest extends BaseTestCase {
  private TestEventDispatchThread edt = new TestEventDispatchThread();

  @Before
  public void setUp() {
    edt.resetOwner();
  }

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

  @Test(expected = IllegalStateException.class)
  public void killFromTask() {
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        edt.kill();
      }
    });
    edt.executeUpdates();
  }

  @Test(expected = IllegalStateException.class)
  public void finishFromTask() {
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        edt.finish();
      }
    });
    edt.executeUpdates();
  }

  @Test
  public void taskExceptionCaught() {
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        throw new UnsupportedOperationException();
      }
    });

    final Value<Boolean> taskExecuted = new Value<>(Boolean.FALSE);
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        taskExecuted.set(Boolean.TRUE);
      }
    });
    assertEquals(2, edt.size());

    ThrowableHandlers.asInProduction(new Runnable() {
      @Override
      public void run() {
        edt.executeUpdates();
      }
    });
    assertTrue(edt.isEmpty());
    assertTrue(taskExecuted.get());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void taskExceptionThrown() {
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        throw new UnsupportedOperationException();
      }
    });
    final Runnable r = Mockito.mock(Runnable.class);
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        edt.schedule(r);
      }
    });

    try {
      edt.executeUpdates();
    } finally {
      assertEquals(1, edt.size());
      Mockito.verify(r, Mockito.never()).run();
    }
  }
}