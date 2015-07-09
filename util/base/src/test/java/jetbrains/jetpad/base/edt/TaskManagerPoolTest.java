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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class TaskManagerPoolTest extends BaseTestCase {
  private TaskManagerPool pool;
  private Set<TaskManager> managers = new HashSet<>();

  private TaskManager createManager() {
    TaskManager manager = pool.createTaskManager("");
    managers.add(manager);
    return manager;
  }

  @After
  public void finish() {
    for (TaskManager manager : managers) {
      if (!manager.isStopped()) {
        manager.finish();
      }
    }
    assertEmptyPool();
  }

  private void assertEmptyPool() {
    assertTrue(pool.isEmpty());
  }

  private void init(int poolSize) {
    pool = new TaskManagerPool("test pool", poolSize, new TaskManagerFactory() {
      @Override
      public TaskManager createTaskManager(String taskManagerName) {
        return new ThreadyTaskManager(taskManagerName);
      }
    });
  }

  private void init() {
    init(1);
  }

  @Test
  public void simpleTask() {
    init();
    TaskManager manager = createManager();
    final Value<Boolean> taskExecuted = new Value<>(false);
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        taskExecuted.set(true);
      }
    });
    manager.finish();

    assertTrue(taskExecuted.get());
  }

  @Test
  public void taskAfterFinished() {
    init();
    TaskManager manager = createManager();
    manager.finish();
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  @Test
  public void killManager() {
    init();
    TaskManager manager = createManager();
    final CountDownLatch latch = new CountDownLatch(1);
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        try {
          latch.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
    manager.kill();
  }

  @Test
  public void twoManagers() {
    init();
    TaskManager manager1 = createManager();
    TaskManager manager2 = createManager();

    final Value<Integer> v = new Value<>(0);
    manager1.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        if (!(v.get() == 0)) {
          throw new IllegalStateException();
        }
        v.set(1);
      }
    });
    manager1.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        if (!(v.get() == 1)) {
          throw new IllegalStateException();
        }
        v.set(2);
      }
    });
    manager1.finish();
    manager2.finish();

    assertEquals(new Integer("2"), v.get());
  }

  @Test
  public void finishOneManager() {
    init();
    TaskManager manager1 = createManager();
    TaskManager manager2 = createManager();

    final Value<Boolean> taskExecuted = new Value<>(false);
    manager2.finish();
    manager1.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        taskExecuted.set(true);
      }
    });
    manager1.finish();

    assertTrue(taskExecuted.get());
  }

  @Test
  public void checkManager() {
    init();
    TaskManager temp1 = createManager();

    TaskManager checking = createManager();

    temp1.finish();
    TaskManager temp2 = createManager();

    assertTrue(pool.checkManager(checking));
  }
}
