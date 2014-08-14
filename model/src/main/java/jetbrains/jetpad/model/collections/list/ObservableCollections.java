package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;

import java.util.List;
import java.util.Set;

public class ObservableCollections {
  public static <ItemT> ObservableList<ItemT> toObservable(List<ItemT> l) {
    ObservableList<ItemT> result = new ObservableArrayList<>();
    result.addAll(l);
    return result;
  }

  public static <ItemT> ObservableSet<ItemT> toObservable(Set<ItemT> s) {
    ObservableSet<ItemT> result = new ObservableHashSet<>();
    result.addAll(s);
    return result;
  }
}
