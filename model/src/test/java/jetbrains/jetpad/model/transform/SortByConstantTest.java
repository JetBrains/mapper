package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SortByConstantTest {
  ObservableList<String> items = new ObservableArrayList<>();
  ObservableList<String> target = new ObservableArrayList<>();

  Registration myReg;

  @Before
  public void setup() {
    Transformer<ObservableList<String>, ObservableList<String>> trans = Transformers.sortByConstant(x -> x, String::compareTo);
    Transformation<ObservableList<String>, ObservableList<String>> transformation = trans.transform(items, target);
    myReg = new Registration() {
      @Override
      protected void doRemove() {
        transformation.dispose();
      }
    };
  }

  @Test
  public void simpleAdds() {
    items.add("a");
    items.add("z");
    items.add("x");

    assertEquals(Arrays.asList("a", "x", "z"), target);
  }

  @Test
  public void simpleRemoves() {
    items.add("a");
    items.add("z");
    items.add("x");

    items.remove("x");

    assertEquals(Arrays.asList("a", "z"), target);
  }

  @Test
  public void dipose() {
    items.add("a");
    items.add("z");

    myReg.remove();

    assertTrue(target.isEmpty());
  }
}
