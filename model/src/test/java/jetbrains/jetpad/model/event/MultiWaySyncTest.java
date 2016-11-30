package jetbrains.jetpad.model.event;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
