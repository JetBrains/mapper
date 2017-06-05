package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;

import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class ObservableWrappers {
  private static <TargetItemT, SourceItemT> CollectionItemEvent<TargetItemT> convertEvent(
      CollectionItemEvent<? extends SourceItemT> event, Function<SourceItemT, TargetItemT> f) {
    TargetItemT oldItem = event.getOldItem() != null ? f.apply(event.getOldItem()) : null;
    TargetItemT newItem = event.getNewItem() != null ? f.apply(event.getNewItem()) : null;
    return new CollectionItemEvent<>(oldItem, newItem, event.getIndex(), event.getType());
  }


  public static class ListMap<SourceItemT, TargetItemT> extends AbstractList<TargetItemT> implements ObservableList<TargetItemT> {
    private final ObservableList<SourceItemT> mySource;
    private final Function<SourceItemT, TargetItemT> myStoT;
    private final Function<TargetItemT, SourceItemT> myTtoS;

    public ListMap(ObservableList<SourceItemT> source, Function<SourceItemT, TargetItemT> toTarget, Function<TargetItemT, SourceItemT> toSource) {
      mySource = source;
      myStoT = toTarget;
      myTtoS = toSource;
    }

    @Override
    public Registration addListener(final CollectionListener<TargetItemT> l) {
      return mySource.addListener(new CollectionListener<SourceItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemAdded(convertEvent(event, myStoT));
        }

        @Override
        public void onItemSet(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemSet(convertEvent(event, myStoT));
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemRemoved(convertEvent(event, myStoT));
        }
      });
    }

    @Override
    public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends TargetItemT>> handler) {
      return mySource.addHandler(new EventHandler<CollectionItemEvent<? extends SourceItemT>>() {
        @Override
        public void onEvent(CollectionItemEvent<? extends SourceItemT> event) {
          handler.onEvent(convertEvent(event, myStoT));
        }
      });
    }

    @Override
    public void add(int index, TargetItemT element) {
      mySource.add(index, myTtoS.apply(element));
    }

    @Override
    public TargetItemT set(int index, TargetItemT element) {
      SourceItemT old = mySource.set(index, myTtoS.apply(element));
      return old != null ? myStoT.apply(old) : null;
    }

    @Override
    public TargetItemT get(int index) {
      return myStoT.apply(mySource.get(index));
    }

    @Override
    public TargetItemT remove(int index) {
      SourceItemT old = mySource.remove(index);
      return old != null ? myStoT.apply(old) : null;
    }

    @Override
    public int size() {
      return mySource.size();
    }
  }

  public static class SetMap<SourceItemT, TargetItemT> implements ObservableSet<TargetItemT> {
    private final ObservableSet<SourceItemT> mySource;
    private final Function<SourceItemT, TargetItemT> myStoT;
    private final Function<TargetItemT, SourceItemT> myTtoS;

    public SetMap(ObservableSet<SourceItemT> source, Function<SourceItemT, TargetItemT> toTarget, Function<TargetItemT, SourceItemT> toSource) {
      mySource = source;
      myStoT = toTarget;
      myTtoS = toSource;
    }

    @Override
    public Registration addListener(final CollectionListener<TargetItemT> l) {
      return mySource.addListener(new CollectionListener<SourceItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemAdded(convertEvent(event, myStoT));
        }

        @Override
        public void onItemSet(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemSet(convertEvent(event, myStoT));
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends SourceItemT> event) {
          l.onItemRemoved(convertEvent(event, myStoT));
        }
      });
    }

    @Override
    public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends TargetItemT>> handler) {
      return mySource.addHandler(new EventHandler<CollectionItemEvent<? extends SourceItemT>>() {
        @Override
        public void onEvent(CollectionItemEvent<? extends SourceItemT> event) {
          handler.onEvent(convertEvent(event, myStoT));
        }
      });
    }

    @Override
    public int size() {
      return mySource.size();
    }

    @Override
    public boolean isEmpty() {
      return mySource.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      if (o == null) return false;
      return mySource.contains(myTtoS.apply((TargetItemT) o));
    }

    @Override
    public Iterator<TargetItemT> iterator() {
      return new Iterator<TargetItemT>() {
        private Iterator<SourceItemT> myIterator = mySource.iterator();

        @Override
        public boolean hasNext() {
          return myIterator.hasNext();
        }

        @Override
        public TargetItemT next() {
          return myStoT.apply(myIterator.next());
        }

        @Override
        public void remove() {
          myIterator.remove();
        }
      };
    }

    @Override
    public Object[] toArray() {
      Object[] result = new Object[mySource.size()];
      int current = 0;
      for (SourceItemT item : mySource) {
        result[current++] = myStoT.apply(item);
      }
      return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return new HashSet<>(this).toArray(a);
    }

    @Override
    public boolean add(TargetItemT wrapper) {
      return mySource.add(myTtoS.apply(wrapper));
    }

    @Override
    public boolean remove(Object o) {
      return mySource.remove(myTtoS.apply((TargetItemT) o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      for (Object o : c) {
        if (!mySource.contains(myTtoS.apply((TargetItemT) o))) return false;
      }
      return true;
    }

    @Override
    public boolean addAll(Collection<? extends TargetItemT> c) {
      boolean changed = false;
      for (TargetItemT w : c) {
        if (add(w)) {
          changed = true;
        }
      }
      return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      Iterator<TargetItemT> it = iterator();
      while (it.hasNext()) {
        TargetItemT current = it.next();
        if (!c.contains(current)) {
          it.remove();
        }
      }
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      boolean changed = false;
      for (Object o : c) {
        if (mySource.remove(myTtoS.apply((TargetItemT) o))) {
          changed = true;
        }
      }
      return changed;
    }

    @Override
    public void clear() {
      mySource.clear();
    }
  }
}
