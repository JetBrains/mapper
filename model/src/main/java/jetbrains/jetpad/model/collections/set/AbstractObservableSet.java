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
package jetbrains.jetpad.model.collections.set;

import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.base.Registration;

import java.util.AbstractSet;

public abstract class AbstractObservableSet<ItemT> extends AbstractSet<ItemT> implements ObservableSet<ItemT> {
  private Listeners<CollectionListener<ItemT>> myListeners;
  
  public Registration addListener(CollectionListener<ItemT> l) {
    if (myListeners == null) {
      myListeners = new Listeners<>();
    }
    return myListeners.add(l);
  }

  protected void checkAdd(ItemT item) {
  }

  protected void checkRemove(ItemT item) {
  }

  protected final void add(final ItemT item, Runnable action) {
    checkAdd(item);

    beforeItemAdded(item);

    boolean success = false;
    try {
      action.run();
      success = true;
      if (myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<ItemT>>() {
          @Override
          public void call(CollectionListener<ItemT> l) {
            l.onItemAdded(new CollectionItemEvent<>(item, -1, true));
          }
        });
      }
    } finally {
      afterItemAdded(item, success);
    }
  }

  protected void beforeItemAdded(ItemT item) {
  }

  protected void afterItemAdded(ItemT item, boolean success) {
  }


  protected final void remove(final ItemT item, Runnable action) {
    checkRemove(item);

    beforeItemRemoved(item);

    boolean success = false;
    try {
      action.run();
      success = true;
      if (myListeners != null) {
        myListeners.fire(new ListenerCaller<CollectionListener<ItemT>>() {
          @Override
          public void call(CollectionListener<ItemT> l) {
            l.onItemRemoved(new CollectionItemEvent<>(item, -1, false));
          }
        });
      }
    } finally {
      afterItemRemoved(item, success);
    }
  }

  protected void beforeItemRemoved(ItemT item) {
  }

  protected void afterItemRemoved(ItemT item, boolean success) {
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends ItemT>> handler) {
    final CollectionListener<ItemT> listener = new CollectionAdapter<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
        handler.onEvent(event);
      }
    };
    return addListener(listener);
  }
}