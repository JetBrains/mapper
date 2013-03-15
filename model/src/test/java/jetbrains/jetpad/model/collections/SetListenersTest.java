package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SetListenersTest {
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

    ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemAdded(Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemAdded(Integer item) {
        num[0]++;
      }
    };
    set.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      set.add(0);
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertTrue(num[0] == 2);
    assertTrue(set.size() == 1);
  }

  @Test
  public void badRemoveListener() {
    final int[] num = new int[]{0};

    ObservableHashSet<Integer> set = new ObservableHashSet<Integer>() {
      @Override
      protected void beforeItemRemoved(Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemRemoved(Integer item) {
        num[0]++;
      }
    };
    set.add(0);
    set.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      set.remove(0);
    } catch (RuntimeException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertTrue(num[0] == 2);
    assertTrue(set.size() == 0);
  }
}
