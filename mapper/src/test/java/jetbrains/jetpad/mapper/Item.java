/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import java.util.List;

class Item {
  public final ObservableList<Item> observableChildren = new ObservableArrayList<Item>();
  public final List<Item> children = new ArrayList<Item>();
  public final ObservableList<Item> transformedChildren = new ObservableArrayList<Item>();
  public final Property<Item> singleChild = new ValueProperty<Item>();
  public final Property<String> name = new ValueProperty<String>();

  Item() {
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name.get(), observableChildren, children, transformedChildren, singleChild.get());
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Item)) {
      return false;
    }
    
    Item otherItem = (Item) object;
    return Objects.equal(name.get(), otherItem.name.get())
      && observableChildren.equals(otherItem.observableChildren)
      && children.equals(otherItem.children)
      && transformedChildren.equals(otherItem.transformedChildren)
      && Objects.equal(singleChild.get(), otherItem.singleChild.get());
  }

  @Override
  public String toString() {
    return "Item " + name.get();
  }
}
