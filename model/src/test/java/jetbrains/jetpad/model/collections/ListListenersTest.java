package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
    final int[] num = new int[]{0, 0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemAdded(int index, Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemAdded(int index, Integer item, boolean success) {
        num[0]++;
        if (success) {
          num[1]++;
        }
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
    assertEquals(1, num[1]);
    assertTrue(num[0] == 2);
    assertTrue(list.size() == 1);
  }

  @Test
  public void badRemoveListener() {
    final int[] num = new int[]{0, 0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
      @Override
      protected void beforeItemRemoved(int index, Integer item) {
        num[0]++;
      }

      @Override
      protected void afterItemRemoved(int index, Integer item, boolean success) {
        num[0]++;
        if (success) {
          num[1]++;
        }
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
    assertEquals(1, num[1]);
    assertTrue(list.size() == 0);
  }

  @Test
  public void addFailure() {
    final int[] num = new int[]{0, 0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
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
        num[0]++;
      }

      @Override
      protected void afterItemAdded(int index, Integer item, boolean success) {
        num[0]++;
        if (success) {
          num[1]++;
        }
      }
    };
    list.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      list.add(0);
    } catch (IllegalStateException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertEquals(0, num[1]);
    assertTrue(num[0] == 2);
    assertTrue(list.isEmpty());
  }

  @Test
  public void removeFailure() {
    final int[] num = new int[]{0, 0};

    ObservableArrayList<Integer> list = new ObservableArrayList<Integer>() {
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
        num[0]++;
      }

      @Override
      protected void afterItemRemoved(int index, Integer item, boolean success) {
        num[0]++;
        if (success) {
          num[1]++;
        }
      }
    };
    list.add(0);
    list.addListener(badListener);

    boolean exceptionOccurred = false;
    try {
      list.remove(0);
    } catch (IllegalStateException e) {
      exceptionOccurred = true;
    }

    assertTrue(exceptionOccurred);
    assertTrue(num[0] == 2);
    assertEquals(0, num[1]);
    assertEquals(1, list.size());
  }
}