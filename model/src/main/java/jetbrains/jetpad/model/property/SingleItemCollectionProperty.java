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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;

public class SingleItemCollectionProperty<ItemT> implements Property<ItemT> {
  private ObservableCollection<ItemT> myCollection;

  public SingleItemCollectionProperty(ObservableCollection<ItemT> collection) {
    myCollection = collection;
  }

  @Override
  public ItemT get() {
    if (myCollection.isEmpty()) {
      return null;
    }
    return myCollection.iterator().next();
  }

  @Override
  public void set(ItemT value) {
    ItemT current = get();
    if (current != null && current.equals(value)) return;
    myCollection.clear();
    if (value != null) {
      myCollection.add(value);
    }
  }

  @Override
  public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ItemT>> handler) {
    return myCollection.addListener(new CollectionAdapter<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<ItemT> event) {
        if (myCollection.size() != 1) {
          throw new IllegalStateException();
        }
        handler.onEvent(new PropertyChangeEvent<>(null, event.getItem()));
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<ItemT> event) {
        if (!myCollection.isEmpty()) {
          throw new IllegalStateException();
        }
        handler.onEvent(new PropertyChangeEvent<>(event.getItem(), null));
      }
    });
  }

  @Override
  public String getPropExpr() {
    return "singleItemCollection(" + myCollection + ")";
  }
}