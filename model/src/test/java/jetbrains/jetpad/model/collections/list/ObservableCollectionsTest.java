package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObservableCollectionsTest extends BaseTestCase {
  @Test
  public void allTest() {
    ObservableCollection<String> collection = new ObservableArrayList<>();
    ReadableProperty<Boolean> all = ObservableCollections.all(collection, new Predicate<String>() {
      @Override
      public boolean test(String s) {
        return s.startsWith("a");
      }
    });
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
    ReadableProperty<Boolean> any = ObservableCollections.any(collection, new Predicate<String>() {
      @Override
      public boolean test(String s) {
        return s.startsWith("a");
      }
    });

    assertFalse(any.get());

    collection.add("b");
    assertFalse(any.get());
    collection.add("a");
    assertTrue(any.get());

    collection.clear();
    assertFalse(any.get());
  }
}
