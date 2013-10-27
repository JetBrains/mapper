package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MapTrasnfomerTest {
  private ObservableList<String> source = new ObservableArrayList<String>();
  private ObservableList<Integer> target;


  @Before
  public void before() {
    Transformer<ObservableList<String>, ObservableList<Integer>> transformer = Transformers.listMap(
      Transformers.fromFun(new Function<String, Integer>() {
        @Override
        public Integer apply(String input) {
          return input.length();
        }
      }));

    target = transformer.transform(source).getTarget();
  }

  @Test
  public void itemAdd() {
    source.add("aaa");
    source.add(0, "b");

    assertEquals(Arrays.asList(1, 3), target);
  }


  @Test
  public void itemRemove() {
    source.addAll(Arrays.asList("aaaa", "bbb", "c"));

    source.remove(1);

    assertEquals(Arrays.asList(4, 1), target);
  }

}
