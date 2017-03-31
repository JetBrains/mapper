/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.model.collections.*;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;

import java.util.*;
import jetbrains.jetpad.base.function.Function;

public class ObservableCollections {
  private static final ObservableList EMPTY_LIST = new AbstractObservableList() {
    @Override
    public int size() {
      return 0;
    }

    @Override
    public Object get(int index) {
      throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    protected void doAdd(int index, Object item) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected void doRemove(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Registration addListener(CollectionListener listener) {
      return Registration.EMPTY;
    }

    @Override
    public Registration addHandler(EventHandler handler) {
      return Registration.EMPTY;
    }
  };

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

  public static <ItemT> WritableProperty<ItemT> asWritableProp(final ObservableCollection<ItemT> coll) {
    return new WritableProperty<ItemT>() {
      @Override
      public void set(ItemT value) {
        coll.clear();
        if (value != null) {
          coll.add(value);
        }
      }
    };
  }

  public static <ItemT> Property<List<ItemT>> asProperty(final ObservableList<ItemT> list) {
    return new Property<List<ItemT>>() {
      @Override
      public String getPropExpr() {
        return "list " + list;
      }

      @Override
      public List<ItemT> get() {
        return Collections.unmodifiableList(new ArrayList<>(list));
      }

      @Override
      public void set(List<ItemT> value) {
        list.clear();
        if (value != null) {
          list.addAll(value);
        }
      }

      @Override
      public Registration addHandler(final EventHandler<? super PropertyChangeEvent<List<ItemT>>> handler) {
        return list.addHandler(new EventHandler<CollectionItemEvent<? extends ItemT>>() {
          List<ItemT> myLastValue = new ArrayList<>(list);

          @Override
          public void onEvent(CollectionItemEvent<? extends ItemT> event) {
            List<ItemT> newValue = new ArrayList<>(list);
            handler.onEvent(new PropertyChangeEvent<>(Collections.unmodifiableList(myLastValue), Collections.unmodifiableList(newValue)));
            myLastValue = newValue;
          }
        });
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <ItemT> ObservableCollection<ItemT> empty() {
    return EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public static <ItemT> ObservableList<ItemT> emptyList() {
    return EMPTY_LIST;
  }

  private static <ItemT> ReadableProperty<Boolean> total(
      final ObservableCollection<ItemT> collection,
      final Predicate<Collection<ItemT>> predicate) {
    return new BaseDerivedProperty<Boolean>(predicate.test(collection)) {
      private Registration myCollectionRegistration;

      @Override
      protected void doAddListeners() {
        myCollectionRegistration = collection.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            somethingChanged();
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            somethingChanged();
          }
        });
      }

      @Override
      protected void doRemoveListeners() {
        myCollectionRegistration.remove();
      }

      @Override
      protected Boolean doGet() {
        return predicate.test(collection);
      }
    };
  }

  public static <ItemT> ReadableProperty<Boolean> all(
      final ObservableCollection<ItemT> collection, final Predicate<? super ItemT> predicate) {
    return total(collection, new Predicate<Collection<ItemT>>() {
      @Override
      public boolean test(Collection<ItemT> value) {
        for (ItemT item : value) {
          if (!predicate.test(item)) {
            return false;
          }
        }
        return true;
      }
    });
  }

  public static <ItemT> ReadableProperty<Boolean> any(
      final ObservableCollection<ItemT> collection, final Predicate<? super ItemT> predicate) {
    return total(collection, new Predicate<Collection<ItemT>>() {
      @Override
      public boolean test(Collection<ItemT> value) {
        for (ItemT item : value) {
          if (predicate.test(item)) {
            return true;
          }
        }
        return false;
      }
    });
  }

  public static <ValueT, ItemT> ObservableCollection<ItemT> selectCollection(
      ReadableProperty<ValueT> p, Function<ValueT, ObservableCollection<ItemT>> s) {
    return new UnmodifiableObservableCollection<>(new SelectorDerivedCollection<>(p, s));
  }

  private static class SelectorDerivedCollection<ValueT, ItemT>
      extends SelectedCollection<ValueT, ItemT, ObservableCollection<ItemT>> {
    public SelectorDerivedCollection(ReadableProperty<ValueT> source, Function<ValueT, ObservableCollection<ItemT>> fun) {
      super(source, fun);
    }

    @Override
    protected ObservableCollection<ItemT> empty() {
      return ObservableCollections.empty();
    }

    @Override
    protected Registration follow(ObservableCollection<ItemT> srcCollection) {
      for (ItemT i : srcCollection) {
        add(i);
      }

      return srcCollection.addListener(new CollectionAdapter<ItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
          add(event.getNewItem());
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
          remove(event.getOldItem());
        }
      });
    }

    @Override
    public boolean contains(Object o) {
      if (isFollowing()) {
        return super.contains(o);
      } else {
        return select().contains(o);
      }
    }

    @Override
    public Iterator<ItemT> iterator() {
      if (isFollowing()) {
        return super.iterator();
      } else {
        return select().iterator();
      }
    }
  }

  public static <ValueT, ItemT> ObservableList<ItemT> selectList(
      ReadableProperty<ValueT> p, Function<ValueT, ObservableList<ItemT>> s) {
    return new UnmodifiableObservableList<>(new SelectorDerivedList<>(p, s));
  }

  private static class SelectorDerivedList<ValueT, ItemT>
      extends SelectedCollection<ValueT, ItemT, ObservableList<ItemT>> {
    public SelectorDerivedList(ReadableProperty<ValueT> source, Function<ValueT, ObservableList<ItemT>> fun) {
      super(source, fun);
    }

    @Override
    protected ObservableList<ItemT> empty() {
      return ObservableCollections.emptyList();
    }

    @Override
    protected Registration follow(ObservableList<ItemT> srcList) {
      for (int i = 0; i < srcList.size(); i++) {
        add(i, srcList.get(i));
      }

      return srcList.addListener(new CollectionAdapter<ItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
          add(event.getIndex(), event.getNewItem());
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
          remove(event.getIndex());
        }
      });
    }

    @Override
    public ItemT get(int index) {
      if (isFollowing()) {
        return super.get(index);
      } else {
        return select().get(index);
      }
    }

    @Override
    public Iterator<ItemT> iterator() {
      if (isFollowing()) {
        return super.iterator();
      } else {
        return select().iterator();
      }
    }
  }
}