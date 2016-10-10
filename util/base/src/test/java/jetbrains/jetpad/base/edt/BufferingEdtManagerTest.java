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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BufferingEdtManagerTest extends BaseTestCase {
  private BufferingEdtManager manager = new BufferingEdtManager();

  @Before
  public void start() {
  }

  @Test
  public void finishInTask() {
    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        manager.finish();
      }
    });
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        taskCompleted.set(true);
      }
    });

    manager.flush();

    assertTrue(taskCompleted.get());
    assertTrue(manager.isStopped());
  }

  @Test
  public void killInsideTask() {
    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        manager.kill();
      }
    });
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        taskCompleted.set(true);
      }
    });

    manager.flush();

    assertFalse(taskCompleted.get());
    assertTrue(manager.isStopped());
  }

  @Test(expected = EdtException.class)
  public void addTaskAfterFinish() {
    final Value<Integer> taskCompleted = new Value<>(0);
    manager.getEdt().schedule(new Runnable() {
      @Override
      public void run() {
        manager.getEdt().schedule(new Runnable() {
          @Override
          public void run() {
            taskCompleted.set(1);
            manager.getEdt().schedule(new Runnable() {
              @Override
              public void run() {
                taskCompleted.set(2);
              }
            });
          }
        });
        manager.finish();
      }
    });

    manager.flush();

    assertEquals(new Integer(1), taskCompleted.get());
    assertTrue(manager.isStopped());
  }
}