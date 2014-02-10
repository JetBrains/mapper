/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.event.Registration;

import java.util.AbstractList;

public abstract class AbstractObservableList<ItemT> extends AbstractList<ItemT> implements ObservableList<ItemT> {
  private Listeners<CollectionListener<ItemT>> myListeners;

  /**
   * Check whether we can add item at index and if not so, throw and exception
   */
  protected void checkAdd(int index, ItemT item) {
  }

  /**
   * Check whether we can remove item at index and if not so, throw and exception
   */
  protected void checkRemove(int index, ItemT item) {
  }

  protected final void add(final int index, final ItemT item, Runnable action) {
    checkAdd(index, item);

    beforeItemAdded(index, item);

    boolean success = false;
    try {
      action.run();
      success = true;
      if (myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<ItemT>>() {
          @Override
          public void call(CollectionListener<ItemT> l) {
            l.onItemAdded(new CollectionItemEvent<>(item, index, true));
          }
        });
      }
    } finally {
      afterItemAdded(index, item, success);
    }
  }

  protected void beforeItemAdded(int index, ItemT item) {
  }

  protected void afterItemAdded(int index, ItemT item, boolean success) {
  }

  protected final void remove(final int index, final ItemT item, Runnable action) {
    checkRemove(index, item);

    beforeItemRemoved(index, item);

    boolean success = false;
    try {
      action.run();
      success = true;
      if (myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<ItemT>>() {
          @Override
          public void call(CollectionListener<ItemT> l) {
            l.onItemRemoved(new CollectionItemEvent<>(item, index, false));
          }
        });
      }
    } finally {
      afterItemRemoved(index, item, success);
    }
  }

  protected void beforeItemRemoved(int index, ItemT item) {
  }

  protected void afterItemRemoved(int index, ItemT item, boolean success) {
  }

  public Registration addListener(CollectionListener<ItemT> listener) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    return myListeners.add(listener);
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<ItemT>> handler) {
    final CollectionListener<ItemT> listener = new CollectionAdapter<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<ItemT> event) {
        handler.onEvent(event);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<ItemT> event) {
        handler.onEvent(event);
      }
    };
    return addListener(listener);
  }
}