package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListListenersTest extends ListenersTestCase {
  @Test
  public void badAddListener() {
    final ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemAdded(int index, Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemAdded(int index, Integer item, boolean success) {
        afterAction(success);
      }
    };
    list.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        list.add(0);
      }
    });

    assertEquals(1, list.size());
    assertions(true);
  }

  @Test
  public void badRemoveListener() {
    final ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemRemoved(int index, Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemRemoved(int index, Integer item, boolean success) {
        afterAction(success);
      }
    };
    list.add(0);
    list.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        list.remove(0);
      }
    });

    assertTrue(list.isEmpty());
    assertions(true);
  }

  @Test
  public void addFailure() {
    final ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      public void add(int index, Integer item) {
        add(index, item, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
      }
      @Override
      protected void beforeItemAdded(int index, Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemAdded(int index, Integer item, boolean success) {
        afterAction(success);
      }
    };
    list.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        list.add(0);
      }
    });

    assertTrue(list.isEmpty());
    assertions(false);
  }

  @Test
  public void removeFailure() {
    final ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      public Integer remove(int index) {
        Integer result = get(index);
        remove(index, result, new Runnable() {
          @Override
          public void run() {
            throw new IllegalStateException();
          }
        });
        return result;
      }
      @Override
      protected void beforeItemRemoved(int index, Integer item) {
        beforeAction();
      }
      @Override
      protected void afterItemRemoved(int index, Integer item, boolean success) {
        afterAction(success);
      }
    };
    list.add(0);
    list.addListener(badListener);

    doTestAction(new Runnable() {
      @Override
      public void run() {
        list.remove(0);
      }
    });

    assertEquals(1, list.size());
    assertions(false);
  }
}