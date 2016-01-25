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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.event.ListenerEvent;

import java.util.Objects;

public class CollectionItemEvent<ItemT> implements ListenerEvent<CollectionListener<ItemT>> {
  public enum EventType {
    ADD, SET, REMOVE
  }
  private ItemT myOldItem;
  private ItemT myNewItem;
  private EventType myType;
  private int myIndex;

  public CollectionItemEvent(ItemT oldItem, ItemT newItem, int index, EventType type) {
    if (EventType.ADD.equals(type) && oldItem != null || EventType.REMOVE.equals(type) && newItem != null) {
      throw new IllegalStateException();
    }
    myOldItem = oldItem;
    myNewItem = newItem;
    myIndex = index;
    myType = type;
  }

  public ItemT getOldItem() {
    return myOldItem;
  }

  public ItemT getNewItem() {
    return myNewItem;
  }

  public int getIndex() {
    return myIndex;
  }

  public EventType getType() {
    return myType;
  }

  @Override
  public void dispatch(CollectionListener<ItemT> l) {
    if (EventType.ADD.equals(myType)) {
      l.onItemAdded(this);
    } else if (EventType.SET.equals(myType)) {
      l.onItemSet(this);
    } else {
      l.onItemRemoved(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CollectionItemEvent that = (CollectionItemEvent) o;

    return Objects.equals(myOldItem, that.myOldItem) && Objects.equals(myNewItem, that.myNewItem) &&
           Objects.equals(myType, that.myType) && myIndex == that.myIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(myOldItem, myNewItem, myType, myIndex);
  }

  @Override
  public String toString() {
    if (EventType.ADD.equals(myType)) {
      return myNewItem + " added at " + myIndex;
    } else if (EventType.SET.equals(myType)) {
      return myOldItem + " replaced with " + myNewItem + " at " + myIndex;
    } else {
      return myOldItem + " removed at " + myIndex;
    }
  }
}