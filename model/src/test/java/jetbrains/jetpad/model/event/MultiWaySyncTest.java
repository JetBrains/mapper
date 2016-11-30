package jetbrains.jetpad.model.event;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiWaySyncTest extends BaseTestCase {
  private MultiWaySync sync = new MultiWaySync();

  @Test
  public void whenInSync() {
    final StringBuilder log = new StringBuilder();
    sync.sync(new Runnable() {
      @Override
      public void run() {
        log.append("start;");
        sync.whenInSync(new Runnable() {
          @Override
          public void run() {
            log.append("whenInSync;");
          }
        });
      }
    });

    assertEquals("start;whenInSync;", log.toString());
  }

  @Test
  public void startAndFinish() {
    sync.startSync();

    assertTrue(sync.isInSync());

    sync.finishSync();

    assertFalse(sync.isInSync());
  }
}
