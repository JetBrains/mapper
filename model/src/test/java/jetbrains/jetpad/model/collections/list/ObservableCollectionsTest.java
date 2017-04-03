package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.base.function.Supplier;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObservableCollectionsTest extends BaseTestCase {

  private static final Predicate<String> STARTS_WITH_A = new Predicate<String>() {
    @Override
    public boolean test(String s) {
      return s.startsWith("a");
    }
  };

  @Test
  public void count() {
    ObservableList<String> collection = new ObservableArrayList<>();
    ReadableProperty<Integer> count = ObservableCollections.count(collection, STARTS_WITH_A);

    runChanges(collection, count);
  }

  @Test
  public void countListener() {
    ObservableList<String> collection = new ObservableArrayList<>();
    ReadableProperty<Integer> count = ObservableCollections.count(collection, STARTS_WITH_A);

    final Value<Integer> lastUpdate = new Value<>(0);
    count.addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Integer> event) {
        lastUpdate.set(event.getNewValue());
      }
    });

    runChanges(collection, lastUpdate);
  }

  @Test
  public void allTest() {
    ObservableCollection<String> collection = new ObservableArrayList<>();
    ReadableProperty<Boolean> all = ObservableCollections.all(collection, STARTS_WITH_A);

    assertTrue(all.get());

    collection.add("a");
    assertTrue(all.get());
    collection.add("b");
    assertFalse(all.get());

    collection.clear();
    assertTrue(all.get());
  }

  @Test
  public void anyTest() {
    ObservableCollection<String> collection = new ObservableArrayList<>();
    ReadableProperty<Boolean> any = ObservableCollections.any(collection, STARTS_WITH_A);

    assertFalse(any.get());

    collection.add("b");
    assertFalse(any.get());
    collection.add("a");
    assertTrue(any.get());

    collection.clear();
    assertFalse(any.get());
  }

  private void runChanges(ObservableList<String> collection, Supplier<Integer> count) {
    assertEquals(Integer.valueOf(0), count.get());

    collection.add("a");
    assertEquals(Integer.valueOf(1), count.get());
    collection.add("b");
    assertEquals(Integer.valueOf(1), count.get());
    collection.add("a");
    assertEquals(Integer.valueOf(2), count.get());
    collection.add("b");
    assertEquals(Integer.valueOf(2), count.get());

    collection.remove(1);
    assertEquals(Integer.valueOf(2), count.get());
    collection.remove(1);
    assertEquals(Integer.valueOf(1), count.get());

    collection.clear();
    assertEquals(Integer.valueOf(0), count.get());
  }
}
