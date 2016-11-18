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
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import java.util.AbstractList;

public abstract class AbstractObservableList<ItemT> extends AbstractList<ItemT> implements ObservableList<ItemT> {
  private Listeners<CollectionListener<ItemT>> myListeners;

  protected void checkAdd(int index, ItemT item) {
    if (index < 0 || index > size()) {
      throw new IndexOutOfBoundsException("Add: index=" + index + ", size=" + size());
    }
  }

  protected void checkSet(int index, ItemT oldItem, ItemT newItem) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("Set: index=" + index + ", size=" + size());
    }
  }

  protected void checkRemove(int index, ItemT item) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("Remove: index=" + index + ", size=" + size());
    }
  }

  @Override
  public final void add(final int index, final ItemT item) {
    checkAdd(index, item);
    beforeItemAdded(index, item);
    boolean success = false;
    try {
      doAdd(index, item);
      success = true;
      onItemAdd(index, item);
      if (myListeners != null) {
        try (Listeners.Firing<CollectionListener<ItemT>> firing = myListeners.fire()) {
          final CollectionItemEvent<ItemT> event = new CollectionItemEvent<>(null, item, index, CollectionItemEvent.EventType.ADD);
          for (CollectionListener<ItemT> l : firing) {
            try {
              l.onItemAdded(event);
            } catch (Throwable t) {
              ThrowableHandlers.handle(t);
            }
          }
        }
      }
    } finally {
      afterItemAdded(index, item, success);
    }
  }

  protected abstract void doAdd(int index, ItemT item);

  protected void beforeItemAdded(int index, ItemT item) {
  }

  protected void onItemAdd(int index, ItemT item) {
  }

  protected void afterItemAdded(int index, ItemT item, boolean success) {
  }

  @Override
  public final ItemT set(final int index, final ItemT item) {
    final ItemT old = get(index);
    checkSet(index, old, item);
    beforeItemSet(index, old, item);
    boolean success = false;
    try {
      doSet(index, item);
      success = true;
      onItemSet(index, old, item);
      if (myListeners != null) {
        final CollectionItemEvent<ItemT> event = new CollectionItemEvent<>(old, item, index, CollectionItemEvent.EventType.SET);
        try (Listeners.Firing<CollectionListener<ItemT>> firing = myListeners.fire()) {
          for (CollectionListener<ItemT> l : firing) {
            try {
              l.onItemSet(event);
            } catch (Throwable t) {
              ThrowableHandlers.handle(t);
            }
          }
        }
      }
    } finally {
      afterItemSet(index, old, item, success);
    }
    return old;
  }

  protected void doSet(int index, ItemT item) {
    doRemove(index);
    doAdd(index, item);
  }

  protected void beforeItemSet(int index, ItemT oldItem, ItemT newItem) {
  }

  protected void onItemSet(int index, ItemT oldItem, ItemT newItem) {
  }

  protected void afterItemSet(int index, ItemT oldItem, ItemT newItem, boolean success) {
  }

  @Override
  public final ItemT remove(final int index) {
    final ItemT item = get(index);
    checkRemove(index, item);
    beforeItemRemoved(index, item);
    boolean success = false;
    try {
      doRemove(index);
      success = true;
      onItemRemove(index, item);
      if (myListeners != null) {
        final CollectionItemEvent<ItemT> event = new CollectionItemEvent<>(item, null, index, CollectionItemEvent.EventType.REMOVE);
        try (Listeners.Firing<CollectionListener<ItemT>> firing = myListeners.fire()) {
          for (CollectionListener<ItemT> l : firing) {
            try {
              l.onItemRemoved(event);
            } catch (Throwable t) {
              ThrowableHandlers.handle(t);
            }
          }
        }
      }
    } finally {
      afterItemRemoved(index, item, success);
    }
    return item;
  }

  protected abstract void doRemove(int index);

  protected void beforeItemRemoved(int index, ItemT item) {
  }

  protected void onItemRemove(int index, ItemT item) {
  }

  protected void afterItemRemoved(int index, ItemT item, boolean success) {
  }

  public Registration addListener(CollectionListener<ItemT> listener) {
    if (myListeners == null) {
      myListeners = new Listeners<CollectionListener<ItemT>>() {
        @Override
        protected void beforeFirstAdded() {
          onListenersAdded();
        }

        @Override
        protected void afterLastRemoved() {
          myListeners = null;
          onListenersRemoved();
        }
      };
    }

    return myListeners.add(listener);
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends ItemT>> handler) {
    final CollectionListener<ItemT> listener = new CollectionListener<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }
    };
    return addListener(listener);
  }

  protected void onListenersAdded() {
  }

  protected void onListenersRemoved() {
  }
}