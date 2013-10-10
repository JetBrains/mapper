package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetListenersTest extends ListenersTestCase {
  @Test
  public void badAddListener() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemAdded(Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemAdded(Integer item, boolean success) {
        afterAction(success);
      }
    };
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.add(0);
      }
    });

    assertEquals(1, set.size());
    assertions(true);
  }

  @Test
  public void badRemoveListener() {
    final ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemRemoved(Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemRemoved(Integer item, boolean success) {
        afterAction(success);
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

    assertTrue(set.isEmpty());
    assertions(true);
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
        beforeAction();
      }
      @Override
      protected void afterItemAdded(Integer item, boolean success) {
        afterAction(success);
      }
    };
    set.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        set.add(0);
      }
    });

    assertTrue(set.isEmpty());
    assertions(false);
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
        beforeAction();
      }
      @Override
      protected void afterItemRemoved(Integer item, boolean success) {
        afterAction(success);
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

    assertEquals(1, set.size());
    assertions(false);
  }
}
