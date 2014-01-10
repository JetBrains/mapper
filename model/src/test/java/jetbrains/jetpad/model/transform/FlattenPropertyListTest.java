package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class FlattenPropertyListTest {
  private ObservableList<Property<String>> list = new ObservableArrayList<Property<String>>();
  private ObservableList<String> flattenedList = new ObservableArrayList<String>();
  private Transformation<ObservableList<Property<String>>, ObservableList<String>> trans;

  @Before
  public void init() {
    list.addAll(Arrays.asList(new ValueProperty<String>("a"), new ValueProperty<String>("b"), new ValueProperty<String>("c")));
    trans = Transformers.<String>flattenPropertyList().transform(list, flattenedList);
  }

  @Test
  public void initialState() {
    assertList("a", "b", "c");
  }

  @Test
  public void propSet() {
    list.get(1).set("z");

    assertList("a", "z", "c");
  }

  @Test
  public void itemAdd() {
    list.add(1, new ValueProperty<String>("x"));

    assertList("a", "x", "b", "c");
  }

  @Test
  public void itemRemove() {
    list.remove(1);

    assertList("a", "c");
  }

  private void assertList(String... vals) {
    assertEquals(Arrays.asList(vals), flattenedList);
  }


}
