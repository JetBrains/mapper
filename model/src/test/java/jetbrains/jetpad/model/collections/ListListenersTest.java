package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ListListenersTest {
  private CollectionListener<Integer> badListener = new CollectionListener<Integer>() {
    @Override
    public void onItemAdded(CollectionItemEvent<Integer> event) {
      throw new RuntimeException();
    }

    @Override
    public void onItemRemoved(CollectionItemEvent<Integer> event) {
      throw new RuntimeException();
    }
  };

  @Test
  public void badAddListener() {
    final int[] num = new int[]{0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemAdded(int index, Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemAdded(int index, Integer item) {
        num[0]++;
      }
    };
    list.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      list.add(0);
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertTrue(num[0] == 2);
    assertTrue(list.size() == 1);
  }

  @Test
  public void badRemoveListener() {
    final int[] num = new int[]{0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemRemoved(int index, Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemRemoved(int index, Integer item) {
        num[0]++;
      }
    };
    list.add(0);
    list.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      list.remove(0);
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertTrue(num[0] == 2);
    assertTrue(list.size() == 0);
  }
}