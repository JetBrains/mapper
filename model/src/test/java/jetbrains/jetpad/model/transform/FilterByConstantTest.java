package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FilterByConstantTest {
  private ObservableSet<String> items = new ObservableHashSet<>();
  private ObservableSet<String> target = new ObservableHashSet<>();
  private Registration myReg;

  @Before
  public void setup() {
    Transformer<ObservableSet<String>, ObservableCollection<String>> trans = Transformers.filterByConstant(input -> input.startsWith("a"));
    Transformation<ObservableSet<String>, ObservableCollection<String>> transformation = trans.transform(items, target);
    myReg = new Registration() {
      @Override
      protected void doRemove() {
        transformation.dispose();
      }
    };
  }

  @Test
  public void simpleAdd() {
    items.add("aaa");
    items.add("bbb");

    assertEquals(1, target.size());
    assertTrue(target.contains("aaa"));
    assertFalse(target.contains("bbb"));
  }

  @Test
  public void simpleRemove() {
    items.add("aaa");
    items.add("bbb");

    items.remove("bbb");

    assertFalse(target.isEmpty());

    items.remove("aaa");

    assertTrue(target.isEmpty());
  }

  @Test
  public void dispose() {
    items.add("aaa");
    items.add("bbb");

    myReg.dispose();

    assertTrue(target.isEmpty());
  }
}
