package jetbrains.jetpad.model.collections;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

public class ListenersTestCase {
  protected boolean beforeHappened = false;
  protected boolean afterHappened = false;
  protected boolean successful = false;
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

  @After
  public void tearDown() {
    assertTrue(exceptionOccurred);
    assertTrue(beforeHappened);
    assertTrue(afterHappened);
  }

  protected void doTestAction(Runnable r) {
    try {
      r.run();
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }
  }
}
