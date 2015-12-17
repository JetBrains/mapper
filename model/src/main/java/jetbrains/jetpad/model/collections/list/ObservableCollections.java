/*
 * Copyright 2012-2015 JetBrains s.r.o
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
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

  public static <ValueT, ItemT> ObservableCollection<ItemT> selectCollection(ReadableProperty<ValueT> p, Selector<ValueT, ObservableCollection<ItemT>> s) {
    return new UnmodifiableObservableList<>(new SelectorDerivedCollection<>(p, s));
  }

  private static class SelectorDerivedCollection<ValueT, ItemT> extends ObservableArrayList<ItemT> implements EventHandler<PropertyChangeEvent<ValueT>> {
    private Registration mySrcPropertyRegistration = Registration.EMPTY;
    private Registration mySrcListRegistration = Registration.EMPTY;

    private final Selector<ValueT, ObservableCollection<ItemT>> mySelector;
    private final ReadableProperty<ValueT> mySource;

    private boolean myFollowing = false;

    public SelectorDerivedCollection(ReadableProperty<ValueT> source, Selector<ValueT, ObservableCollection<ItemT>> fun) {
      mySource = source;
      mySelector = fun;
    }

    @Override
    public void onEvent(PropertyChangeEvent<ValueT> event) {
      if (event.getOldValue() != null) {
        clear();
      }

      observeSelected(doSelect());
    }

    private ObservableCollection<ItemT> doSelect() {
      ValueT sourceVal = mySource.get();
      if (sourceVal != null) {
        ObservableCollection<ItemT> res = mySelector.select(sourceVal);
        if (res != null) return res;
      }

      return empty();
    }

    private void observeSelected(ObservableCollection<ItemT> srcCollection) {
      mySrcListRegistration.remove();

      for (ItemT i : srcCollection) {
        add(i);
      }

      mySrcListRegistration = srcCollection.addListener(new CollectionAdapter<ItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
          add(event.getItem());
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
          remove(event.getItem());
        }
      });
    }

    @Override
    public boolean contains(Object o) {
      if (myFollowing) {
        return super.contains(o);
      } else {
        return doSelect().contains(o);
      }
    }

    @Override
    public int size() {
      if (myFollowing) {
        return super.size();
      } else {
        return doSelect().size();
      }
    }

    @Override
    protected void onListenersAdded() {
      mySrcPropertyRegistration = mySource.addHandler(this);
      observeSelected(doSelect());
      myFollowing = true;
    }

    @Override
    protected void onListenersRemoved() {
      mySrcPropertyRegistration.remove();
      mySrcListRegistration.remove();
      myFollowing = false;
    }
  }

  public static <ValueT, ItemT> ObservableList<ItemT> selectList(ReadableProperty<ValueT> p, Selector<ValueT, ObservableList<ItemT>> s) {
    return new UnmodifiableObservableList<>(new SelectorDerivedList<>(p, s));
  }

  private static class SelectorDerivedList<ValueT, ItemT> extends ObservableArrayList<ItemT> implements EventHandler<PropertyChangeEvent<ValueT>> {
    private Registration mySrcPropertyRegistration = Registration.EMPTY;
    private Registration mySrcListRegistration = Registration.EMPTY;

    private final Selector<ValueT, ObservableList<ItemT>> mySelector;
    private final ReadableProperty<ValueT> mySource;

    private boolean myFollowing = false;

    public SelectorDerivedList(ReadableProperty<ValueT> source, Selector<ValueT, ObservableList<ItemT>> fun) {
      mySource = source;
      mySelector = fun;
    }

    @Override
    public void onEvent(PropertyChangeEvent<ValueT> event) {
      if (event.getOldValue() != null) {
        clear();
      }

      observeSelected(doSelect());
    }

    private ObservableList<ItemT> doSelect() {
      ValueT sourceVal = mySource.get();
      if (sourceVal != null) {
        ObservableList<ItemT> res = mySelector.select(sourceVal);
        if (res != null) return res;
      }

      return emptyList();
    }

    private void observeSelected(ObservableList<ItemT> srcList) {
      mySrcListRegistration.remove();

      for (int i=0; i<srcList.size(); i++) {
        add(i, srcList.get(i));
      }

      mySrcListRegistration = srcList.addListener(new CollectionAdapter<ItemT>() {
        @Override
        public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
          add(event.getIndex(), event.getItem());
        }

        @Override
        public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
          remove(event.getIndex());
        }
      });
    }

    @Override
    public ItemT get(int index) {
      if (myFollowing) {
        return super.get(index);
      } else {
        return doSelect().get(index);
      }
    }

    @Override
    public int size() {
      if (myFollowing) {
        return super.size();
      } else {
        return doSelect().size();
      }
    }

    @Override
    protected void onListenersAdded() {
      mySrcPropertyRegistration = mySource.addHandler(this);
      observeSelected(doSelect());
      myFollowing = true;
    }

    @Override
    protected void onListenersRemoved() {
      mySrcPropertyRegistration.remove();
      mySrcListRegistration.remove();
      myFollowing = false;
    }
  }
}
