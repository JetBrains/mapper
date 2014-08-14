package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableSet;

import java.util.List;
import java.util.Set;

public class CollectionBinding {
  public static <ItemT> Registration bindOneWay(final ObservableList<ItemT> source, final List<ItemT> target) {
    target.addAll(source);
    return source.addListener(new CollectionListener<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<ItemT> event) {
        target.add(event.getIndex(), event.getItem());
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<ItemT> event) {
        target.remove(event.getIndex());
      }
    });
  }

  public static <ItemT> Registration bindOneWay(final ObservableSet<ItemT> source, final Set<ItemT> target) {
    target.addAll(source);
    return source.addListener(new CollectionListener<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<ItemT> event) {
        target.add(event.getItem());
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<ItemT> event) {
        target.remove(event.getItem());
      }
    });
  }
}
