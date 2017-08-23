/*
 * Copyright 2012-2017 JetBrains s.r.o
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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

/**
 * Property which represents a value in an observable list at particular index
 */
public final class ListItemProperty<ValueT> extends BaseReadableProperty<ValueT> implements Property<ValueT>, Disposable {
  private ObservableList<ValueT> myList;
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> myHandlers = new Listeners<>();
  private Registration myReg;
  private boolean myDisposed = false;

  public final Property<Integer> index = new ValueProperty<>();

  public ListItemProperty(ObservableList<ValueT> list, int index) {
    if (index < 0 || index >= list.size()) {
      throw new IndexOutOfBoundsException("Can’t point to a non-existent item");
    }
    this.myList = list;
    this.index.set(index);

    myReg = list.addListener(new CollectionAdapter<ValueT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() <= ListItemProperty.this.index.get()) {
          ListItemProperty.this.index.set(ListItemProperty.this.index.get() + 1);
        }
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() == ListItemProperty.this.index.get()) {
          final PropertyChangeEvent<ValueT> e = new PropertyChangeEvent<>(event.getOldItem(), event.getNewItem());
          myHandlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
            @Override
            public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
              l.onEvent(e);
            }
          });
        }
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() < ListItemProperty.this.index.get()) {
          ListItemProperty.this.index.set(ListItemProperty.this.index.get() - 1);
        } else if (event.getIndex() == ListItemProperty.this.index.get()) {
          invalidate();
          final PropertyChangeEvent<ValueT> e = new PropertyChangeEvent<>(event.getOldItem(), null);
          myHandlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
            @Override
            public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
              l.onEvent(e);
            }
          });
        }
      }
    });
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    return myHandlers.add(handler);
  }

  @Override
  public ValueT get() {
    if (isValid()) {
      return myList.get(index.get());
    } else {
      return null;
    }
  }

  @Override
  public void set(ValueT value) {
    if (isValid()) {
      myList.set(index.get(), value);
    } else {
      throw new IllegalStateException("Property points to an invalid item, can’t set");
    }
  }

  public boolean isValid() {
    return index.get() != null;
  }

  protected void invalidate() {
    index.set(null);
    myReg.dispose();
  }

  @Override
  public void dispose() {
    if (myDisposed) {
      throw new IllegalStateException("Double dispose");
    }
    if (isValid()) {
      myReg.dispose();
    }
    myDisposed = true;
  }
}