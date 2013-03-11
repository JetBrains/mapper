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
package jetbrains.jetpad.model.collections.list;

public class ObservableSingleItemList<ItemT> extends AbstractObservableList<ItemT> {
  private ItemT myItem;

  public ObservableSingleItemList() {
  }

  public ObservableSingleItemList(ItemT item) {
    myItem = item;
  }

  public ItemT getItem() {
    return myItem;
  }

  public void setItem(ItemT item) {
    set(0, item);
  }
  
  @Override
  public ItemT get(int index) {
    if (myItem == null || index != 0) throw new IndexOutOfBoundsException();
    return myItem;
  }

  @Override
  public int size() {
    return myItem == null ? 0 : 1;
  }

  @Override
  public ItemT set(int index, ItemT t) {
    ItemT oldValue = myItem == null ? null : remove(index);
    if (t != null) {
      add(index, t);
    }
    return oldValue;
  }

  @Override
  public void add(int index, final ItemT item) {
    if (size() != 0) throw new IllegalStateException();
    add(index, item, new Runnable() {
      @Override
      public void run() {
        myItem = item;
      }
    });
  }

  @Override
  public ItemT remove(int index) {
    if (index != 0) throw new IndexOutOfBoundsException();
    ItemT oldItem = myItem;
    if (oldItem == null) throw new IndexOutOfBoundsException();

    remove(index, oldItem, new Runnable() {
      @Override
      public void run() {
        myItem = null;
      }
    });
    return oldItem;
  }
}
