package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SetListenersTest extends ListenersTestCase {
  @Test
  public void badAddListener() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemAdded(Integer item) {
        beforeHappened = true;
      }
      @Override
      protected void afterItemAdded(Integer item, boolean success) {
        afterHappened = true;
        successful = success;
      }
    };
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.add(0);
      }
    });

    assertTrue(successful);
    assertEquals(1, set.size());
  }

  @Test
  public void badRemoveListener() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemRemoved(Integer item) {
        beforeHappened = true;
      }
      @Override
      protected void afterItemRemoved(Integer item, boolean success) {
        afterHappened = true;
        successful = success;
      }
    };
    set.add(0);
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.remove(0);
      }
    });

    assertTrue(successful);
    assertTrue(set.isEmpty());
  }

  @Test
  public void addFailure() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      public boolean add(Integer integer) {
        add(integer, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
        return true;
      }

      @Override
      protected void beforeItemAdded(Integer item) {
        beforeHappened = true;
      }
      @Override
      protected void afterItemAdded(Integer item, boolean success) {
        afterHappened = true;
        successful = success;
      }
    };
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.add(0);
      }
    });

    assertFalse(successful);
    assertTrue(set.isEmpty());
  }

  @Test
  public void removeFailure() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      public boolean remove(Object integer) {
        remove((Integer) integer, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
        return true;
      }
      @Override
      protected void beforeItemRemoved(Integer item) {
        beforeHappened = true;
      }
      @Override
      protected void afterItemRemoved(Integer item, boolean success) {
        afterHappened = true;
        successful = success;
      }
    };
    set.add(0);
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.remove(0);
      }
    });

    assertFalse(successful);
    assertEquals(1, set.size());
  }
}
