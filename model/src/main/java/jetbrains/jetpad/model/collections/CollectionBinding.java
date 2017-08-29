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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableSet;

import java.util.List;
import java.util.Set;

public final class CollectionBinding {
  public static <ItemT> Registration bindOneWay(ObservableList<ItemT> source, final List<ItemT> target) {
    target.addAll(source);
    return source.addListener(new CollectionListener<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
        target.add(event.getIndex(), event.getNewItem());
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends ItemT> event) {
        target.set(event.getIndex(), event.getNewItem());
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
        target.remove(event.getIndex());
      }
    });
  }

  public static <ItemT> Registration bindOneWay(ObservableSet<ItemT> source, final Set<ItemT> target) {
    target.addAll(source);
    return source.addListener(new CollectionAdapter<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
        target.add(event.getNewItem());
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
        target.remove(event.getOldItem());
      }
    });
  }

  private CollectionBinding() {
  }
}