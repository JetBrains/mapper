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
package jetbrains.jetpad.model.collections.list;

public class ObservableSingleItemList<ItemT> extends AbstractObservableList<ItemT> {
  private ItemT myItem;
  private boolean myEmpty = true;

  public ObservableSingleItemList() {
  }

  public ObservableSingleItemList(ItemT item) {
    myItem = item;
    myEmpty = false;
  }

  public ItemT getItem() {
    return get(0);
  }

  public void setItem(ItemT item) {
    if (myEmpty) {
      add(item);
    } else {
      set(0, item);
    }
  }
  
  @Override
  public ItemT get(int index) {
    if (myEmpty || index != 0) {
      throw new IndexOutOfBoundsException();
    }
    return myItem;
  }

  @Override
  public int size() {
    return myEmpty ? 0 : 1;
  }

  @Override
  protected void checkAdd(int index, ItemT item) {
    super.checkAdd(index, item);
    if (!myEmpty) {
      throw new IllegalStateException("Single item list already has an item");
    }
  }

  @Override
  protected void checkSet(int index, ItemT oldItem, ItemT newItem) {
    super.checkRemove(index, oldItem);
  }

  @Override
  protected void doAdd(int index, ItemT item) {
    myItem = item;
    myEmpty = false;
  }

  @Override
  protected void doSet(int index, ItemT item) {
    myItem = item;
  }

  @Override
  protected void doRemove(int index) {
    myItem = null;
    myEmpty = true;
  }
}