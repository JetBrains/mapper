package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Test;


public class SingleItemCollectionPropertyTest {
  @Test(expected = IllegalStateException.class)
  public void wrapTooBigCollection() {
    ObservableList<Integer> list = new ObservableArrayList<>();
    list.add(0);
    list.add(1);
    new SingleItemCollectionProperty<>(list);
  }
}
