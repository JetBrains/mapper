package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BufferingTaskManagerTest extends BaseTestCase {
  private BufferingTaskManager manager = new BufferingTaskManager();

  @Before
  public void start() {
  }

  @Test
  public void finishInTask() {
    final Value<Boolean> taskCompleted = new Value<>(false);
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        manager.finish();
      }
    });
    manager.getEDT().schedule(new Runnable() {
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
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        manager.kill();
      }
    });
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        taskCompleted.set(true);
      }
    });

    manager.flush();

    assertFalse(taskCompleted.get());
    assertTrue(manager.isStopped());
  }

  @Test
  public void addTaskAfterFinish() {
    final Value<Integer> taskCompleted = new Value<>(0);
    manager.getEDT().schedule(new Runnable() {
      @Override
      public void run() {
        manager.getEDT().schedule(new Runnable() {
          @Override
          public void run() {
            taskCompleted.set(1);
            manager.getEDT().schedule(new Runnable() {
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

