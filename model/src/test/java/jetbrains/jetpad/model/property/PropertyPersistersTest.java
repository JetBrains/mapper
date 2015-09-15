package jetbrains.jetpad.model.property;

import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Persister;
import jetbrains.jetpad.base.Persisters;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.base.Persisters.stringPersister;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyPersistersTest {

  @Test
  public void nullStringValueProperty() {
    testNull(PropertyPersisters.valuePropertyPersister(Persisters.stringPersister()));
  }

  @Test
  public void valuePropertyEmptyString() {
    assertTrue(PropertyPersisters.valuePropertyPersister(Persisters.stringPersister()).deserialize("").get() == null);
  }

  @Test
  public void nullListOfStringProperties() {
    testNull(propListPersister);
  }

  @Test
  public void listOfStringProperties() {
    List<Property<String>> testList = new ArrayList<>();
    testList.add(new ValueProperty<>("hello"));
    testList.add(new ValueProperty<>(":world,!2312:fds,v;"));
    assertPropListEquals(testList, propListPersister.deserialize(propListPersister.serialize(testList)));
  }

  @Test
  public void listOfStringPropertiesWithNulls() {
    List<Property<String>> testList = new ArrayList<>();
    testList.add(new ValueProperty<String>(null));
    testList.add(null);
    assertPropListEquals(testList, propListPersister.deserialize(propListPersister.serialize(testList)));
  }


  private Persister<List<Property<String>>> propListPersister =
      Persisters.listPersister(PropertyPersisters.valuePropertyPersister(stringPersister()),
          new Supplier<List<Property<String>>>() {
            @Override
            public List<Property<String>> get() {
              return new ArrayList<>();
            }
          });

  private <T> void testNull(Persister<T> persister) {
    T defaultValue = persister.deserialize(null);
    assertEquals(defaultValue, persister.deserialize(persister.serialize(null)));
  }

  private void assertPropListEquals(List<Property<String>> expected, List<Property<String>> actual) {
    for (int i = 0; i < expected.size(); i++) {
      Property<String> left = expected.get(i);
      Property<String> right = actual.get(i);
      assertTrue((left == null) == (right == null));
      if (left != null) {
        assertEquals(left.get(), right.get());
      }
    }
  }
}