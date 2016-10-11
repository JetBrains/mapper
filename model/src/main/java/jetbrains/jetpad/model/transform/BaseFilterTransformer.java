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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

abstract class BaseFilterTransformer <ItemT, CollectionS extends ObservableCollection<ItemT>, CollectionT extends ObservableCollection<ItemT>> extends BaseTransformer<CollectionS, CollectionT> {
  private final Function<ItemT, ReadableProperty<Boolean>> myFilterBy;

  protected abstract void add(ItemT item, CollectionS from, CollectionT to);
  protected abstract CollectionT createTo();

  BaseFilterTransformer(final Function<ItemT, ReadableProperty<Boolean>> filterBy) {
    myFilterBy = filterBy;
  }

  @Override
  public Transformation<CollectionS, CollectionT> transform(CollectionS from) {
    return transform(from, createTo());
  }

  @Override
  public Transformation<CollectionS, CollectionT> transform(final CollectionS from, final CollectionT to) {
    return new Transformation<CollectionS, CollectionT>() {
      private Map<ItemT, Registration> myPropertyRegistrations = new HashMap<>();
      private Registration myCollectionRegistration;

      {
        for (ItemT item : from) {
          boolean shouldAdd = watch(item);
          if (shouldAdd) {
            to.add(item);
          }
        }

        myCollectionRegistration = from.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            ItemT item = event.getNewItem();
            boolean shouldAdd = watch(item);
            if (shouldAdd) {
              add(item, from, to);
            }
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            ItemT item = event.getOldItem();
            unwatch(item);
            to.remove(item);
          }
        });
      }

      private boolean watch(final ItemT item) {
        ReadableProperty<Boolean> property = myFilterBy.apply(item);
        myPropertyRegistrations.put(item, property.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            if (event.getNewValue()) {
              add(item, from, to);
            } else {
              to.remove(item);
            }
          }
        }));
        Boolean value = property.get();
        return value != null && value;
      }

      private void unwatch(ItemT item) {
        myPropertyRegistrations.remove(item).remove();
      }

      @Override
      public CollectionS getSource() {
        return from;
      }

      @Override
      public CollectionT getTarget() {
        return to;
      }

      @Override
      protected void doDispose() {
        myCollectionRegistration.remove();
        for (ItemT item : from) {
          unwatch(item);
        }
      }
    };
  }
}