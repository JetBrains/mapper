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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.event.ListenerEvent;

public class CollectionItemEvent<ItemT> implements ListenerEvent<CollectionListener<ItemT>> {
  private ItemT myItem;
  private boolean myAdded;
  private int myIndex;

  public CollectionItemEvent(ItemT item, int index, boolean added) {
    myItem = item;
    myIndex = index;
    myAdded = added;
  }

  public ItemT getItem() {
    return myItem;
  }

  public int getIndex() {
    return myIndex;
  }

  public boolean isAdded() {
    return myAdded;
  }

  @Override
  public void dispatch(CollectionListener<ItemT> l) {
    if (myAdded) {
      l.onItemAdded(this);
    } else {
      l.onItemRemoved(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CollectionItemEvent that = (CollectionItemEvent) o;

    if (myAdded != that.myAdded) return false;
    if (myIndex != that.myIndex) return false;
    if (myItem != null ? !myItem.equals(that.myItem) : that.myItem != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myItem != null ? myItem.hashCode() : 0;
    result = 31 * result + (myAdded ? 1 : 0);
    result = 31 * result + myIndex;
    return result;
  }
}