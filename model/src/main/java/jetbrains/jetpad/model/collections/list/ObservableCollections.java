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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ObservableCollections {
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
        return list.addHandler(new EventHandler<CollectionItemEvent<ItemT>>() {
          List<ItemT> myLastValue = new ArrayList<>(list);

          @Override
          public void onEvent(CollectionItemEvent<ItemT> event) {
            List<ItemT> newValue = new ArrayList<>(list);
            handler.onEvent(new PropertyChangeEvent<>(Collections.unmodifiableList(myLastValue), Collections.unmodifiableList(newValue)));
            myLastValue = newValue;
          }
        });
      }
    };
  }
}