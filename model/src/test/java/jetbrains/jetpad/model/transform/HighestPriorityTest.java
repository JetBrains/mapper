package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HighestPriorityTest {
  private static final Function<String, Integer> PRIORITY_BY_LENGTH = new Function<String, Integer>() {
    @Override
    public Integer apply(String s) {
      return s.length();
    }
  };

  private ObservableCollection<String> from;
  private Transformation<ObservableCollection<String>, ObservableCollection<String>> trans;
  private ObservableCollection<String> to;

  @Before
  public void init() {
    from = new ObservableArrayList<>();
    from.addAll(Arrays.asList("a", "b", "cc"));
    trans = Transformers.highestPriority(PRIORITY_BY_LENGTH).transform(from);
    to = trans.getTarget();
  }

  @Test
  public void initialState() {
    assertItems("cc");
  }

  @Test
  public void addLowerPriority() {
    from.add("c");
    assertItems("cc");
  }

  @Test
  public void addHighPriority() {
    from.add("dd");
    assertItems("cc", "dd");
  }

  @Test
  public void addHigherPriority() {
    from.add("dddd");
    assertItems("dddd");
  }

  @Test
  public void addDifferentPriorities() {
    from.addAll(Arrays.asList("d", "dd"));
    assertItems("cc", "dd");
  }

  @Test
  public void removeLowerPriority() {
    from.remove("a");
    assertItems("cc");
  }

  @Test
  public void removeHighPriority() {
    from.remove("cc");
    assertItems("a", "b");
  }

  @Test
  public void addRemove() {
    from.add("dddd");
    from.remove("dddd");
    assertItems("cc");
    from.remove("cc");
    assertItems("a", "b");
  }

  @Test
  public void clear() {
    from.removeAll(Arrays.asList("a", "b", "cc"));
    assertItems();
  }

  @Test
  public void dispose() {
    trans.dispose();
    from.add("dddd");
    assertItems("cc");
    from.remove("cc");
    assertItems("cc");
  }

  private void assertItems(String ... items) {
    assertEquals(items.length, to.size());
    assertTrue(to.containsAll(Arrays.asList(items)));
  }
}
