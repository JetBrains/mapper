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
package jetbrains.jetpad.model.collections.set;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;

public abstract class AbstractObservableSet<ItemT> extends AbstractSet<ItemT> implements ObservableSet<ItemT> {
  private Listeners<CollectionListener<? super ItemT>> myListeners;

  @Override
  public Registration addListener(CollectionListener<? super ItemT> l) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    return myListeners.add(l);
  }

  @Override
  public final boolean add(ItemT item) {
    if (contains(item)) return false;
    doBeforeAdd(item);
    boolean success = false;
    try {
      onItemAdd(item);
      success = doAdd(item);
    } finally {
      doAfterAdd(item, success);
    }
    return success;
  }

  private void doBeforeAdd(ItemT item) {
    checkAdd(item);
    beforeItemAdded(item);
  }

  private void doAfterAdd(final ItemT item, boolean success) {
    try {
      if (success && myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<? super ItemT>>() {
          @Override
          public void call(CollectionListener<? super ItemT> l) {
            l.onItemAdded(new CollectionItemEvent<>(null, item, -1, CollectionItemEvent.EventType.ADD));
          }
        });
      }
    } finally {
      afterItemAdded(item, success);
    }
  }

  @Override
  public final boolean remove(Object o) {
    if (!contains(o)) return false;
    ItemT item = (ItemT) o;
    doBeforeRemove(item);
    boolean success = false;
    try {
      onItemRemove(item);
      success = doRemove(item);
    } finally {
      doAfterRemove(item, success);
    }
    return success;
  }

  @Override
  public final Iterator<ItemT> iterator() {
    if (size() == 0) {
      return Collections.<ItemT>emptySet().iterator();
    }
    final Iterator<ItemT> iterator = getIterator();
    return new Iterator<ItemT>() {
      private boolean myCanRemove = false;
      private ItemT myLastReturned;

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public ItemT next() {
        myLastReturned = iterator.next();
        myCanRemove = true;
        return myLastReturned;
      }

      @Override
      public void remove() {
        if (!myCanRemove) {
          throw new IllegalStateException();
        }
        myCanRemove = false;
        doBeforeRemove(myLastReturned);
        boolean success = false;
        try {
          iterator.remove();
          success = true;
        } finally {
          doAfterRemove(myLastReturned, success);
        }
      }
    };
  }

  private void doBeforeRemove(ItemT item) {
    checkRemove(item);
    beforeItemRemoved(item);
  }

  private void doAfterRemove(final ItemT item, boolean success) {
    try {
      if (success && myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<? super ItemT>>() {
          @Override
          public void call(CollectionListener<? super ItemT> l) {
            l.onItemRemoved(new CollectionItemEvent<>(item, null, -1, CollectionItemEvent.EventType.REMOVE));
          }
        });
      }
    } finally {
      afterItemRemoved(item, success);
    }
  }

  protected abstract boolean doAdd(ItemT item);
  protected abstract boolean doRemove(ItemT item);
  protected abstract Iterator<ItemT> getIterator();

  protected void checkAdd(ItemT item) {
  }

  protected void checkRemove(ItemT item) {
  }

  protected void beforeItemAdded(ItemT item) {
  }

  protected void onItemAdd(ItemT item) {
  }

  protected void afterItemAdded(ItemT item, boolean success) {
  }

  protected void beforeItemRemoved(ItemT item) {
  }

  protected void onItemRemove(ItemT item) {
  }

  protected void afterItemRemoved(ItemT item, boolean success) {
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends ItemT>> handler) {
    return addListener(new CollectionAdapter<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }
    });
  }
}