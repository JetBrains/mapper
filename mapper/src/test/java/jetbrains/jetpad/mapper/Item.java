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
package jetbrains.jetpad.mapper;

import com.google.common.base.Objects;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Item {
  private static boolean contentsEqual(Item item1, Item item2) {
    return (item1 == item2) || (item1 != null && item1.contentEquals(item2));
  }

  private static boolean contentsEqual(List<Item> list1, List<Item> list2) {
    if (list1.size() != list2.size()) {
      return true;
    }
    Iterator<Item> itr1 = list1.iterator();
    Iterator<Item> itr2 = list2.iterator();
    while (itr1.hasNext()) {
      if (!contentsEqual(itr1.next(), itr2.next())) return false;
    }
    return true;
  }

  public final ObservableList<Item> observableChildren = new ObservableArrayList<>();
  public final List<Item> children = new ArrayList<>();
  public final ObservableList<Item> transformedChildren = new ObservableArrayList<>();
  public final Property<Item> singleChild = new ValueProperty<>();
  public final Property<String> name = new ValueProperty<>();

  Item() {
  }

  @Override
  public String toString() {
    return "Item " + name.get();
  }

  boolean contentEquals(Item item) {
    return Objects.equal(name.get(), item.name.get())
        && contentsEqual(observableChildren, item.observableChildren)
        && contentsEqual(children, item.children)
        && contentsEqual(transformedChildren, item.transformedChildren)
        && contentsEqual(singleChild.get(), item.singleChild.get());
  }
}