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
}
