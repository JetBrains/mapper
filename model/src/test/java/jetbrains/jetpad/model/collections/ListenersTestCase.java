package jetbrains.jetpad.model.collections;

import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListenersTestCase {
  private boolean beforeHappened = false;
  private boolean afterHappened = false;
  private boolean successful = false;
  private boolean exceptionOccurred = false;

  protected final CollectionListener<Integer> badListener = new CollectionListener<Integer>() {
    @Override
    public void onItemAdded(CollectionItemEvent<Integer> event) {
      throw new RuntimeException();
    }
    @Override
    public void onItemRemoved(CollectionItemEvent<Integer> event) {
      throw new RuntimeException();
    }
  };

  @Before
  public void setUp() {
    beforeHappened = false;
    afterHappened = false;
    successful = false;
    exceptionOccurred = false;
  }

  protected void assertions(boolean expectedSuccess) {
    assertEquals(expectedSuccess, successful);
    assertTrue(exceptionOccurred);
    assertTrue(beforeHappened);
    assertTrue(afterHappened);
  }

  protected void beforeAction() {
    beforeHappened = true;
  }

  protected void afterAction(boolean success) {
    afterHappened = true;
    successful = success;
  }

  protected void doTestAction(Runnable r) {
    try {
      r.run();
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }
  }
}
