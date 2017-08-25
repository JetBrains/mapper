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

import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RunningEdtManagerTest extends BaseTestCase {
  private RunningEdtManager manager = new RunningEdtManager();

  @Test
  public void sequentialAddition() {
    final StringBuilder result = new StringBuilder();

    manager.schedule(new Runnable() {
      @Override
      public void run() {
        result.append(1);
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            result.append(3);
            manager.schedule(new Runnable() {
              @Override
              public void run() {
                result.append(5);
              }
            });
            result.append(4);
          }
        });
        result.append(2);
      }
    });
    assertEquals("12345", result.toString());
    assertTrue(manager.isEmpty());
  }

  @Test
  public void exceptionInTask() {
    ThrowableHandlers.asInProduction(new Runnable() {
      @Override
      public void run() {
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            throw new RuntimeException();
          }
        });
      }
    });

    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.schedule(new Runnable() {
      @Override
      public void run() {
        taskCompleted.set(true);
      }
    });

    assertTrue(taskCompleted.get());
  }

  @Test
  public void exceptionDuringFlush() {
    final Value<Integer> performedTasksCounter = new Value<>(0);
    ThrowableHandlers.asInProduction(new Runnable() {
      @Override
      public void run() {
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            manager.schedule(new Runnable() {
              @Override
              public void run() {
                performedTasksCounter.set(performedTasksCounter.get() + 1);
              }
            });
            manager.schedule(new Runnable() {
              @Override
              public void run() {
                throw new RuntimeException();
              }
            });
            manager.schedule(new Runnable() {
              @Override
              public void run() {
                performedTasksCounter.set(performedTasksCounter.get() + 1);
              }
            });
          }
        });
      }
    });

    assertEquals(2, (int) performedTasksCounter.get());
    assertEquals(0, manager.size());
  }

  @Test(expected = IllegalStateException.class)
  public void recursiveFlush() {
    final Value<Integer> performedTasksCounter = new Value<>(0);
    try {
      manager.schedule(new Runnable() {
        @Override
        public void run() {
          manager.schedule(new Runnable() {
            @Override
            public void run() {
              manager.schedule(new Runnable() {
                @Override
                public void run() {
                  performedTasksCounter.set(performedTasksCounter.get() + 1);
                  manager.flush();
                }
              });
              performedTasksCounter.set(performedTasksCounter.get() + 1);
            }
          });
          performedTasksCounter.set(performedTasksCounter.get() + 1);
        }
      });
    } finally {
      assertEquals(3, (int)performedTasksCounter.get());
      assertTrue(manager.isEmpty());
    }
  }

  @Test
  public void finishInsideTask() {
    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.schedule(new Runnable() {
      @Override
      public void run() {
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            taskCompleted.set(true);
          }
        });
        manager.finish();
      }
    });
    assertTrue(taskCompleted.get());
    assertTrue(manager.isStopped());
  }

  @Test
  public void killInsideTask() {
    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.schedule(new Runnable() {
      @Override
      public void run() {
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            taskCompleted.set(true);
          }
        });
        manager.kill();
      }
    });
    assertFalse(taskCompleted.get());
    assertTrue(manager.isStopped());
  }

  @Test
  public void addTaskAfterFinish() {
    final Value<Integer> taskCompleted = new Value<>(0);
    manager.schedule(new Runnable() {
      @Override
      public void run() {
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            taskCompleted.set(1);
            manager.schedule(new Runnable() {
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
    assertEquals(1, (int) taskCompleted.get());
    assertTrue(manager.isStopped());
  }
}