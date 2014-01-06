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
package jetbrains.jetpad.model.collections;

import java.util.AbstractList;

public abstract class SingleItemList<ItemT> extends AbstractList<ItemT> {
  protected abstract ItemT getItem();

  protected abstract void setItem(ItemT item);

  @Override
  public ItemT get(int index) {
    ItemT item = getItem();
    if (item == null || index != 0) throw new IndexOutOfBoundsException();
    return item;
  }

  @Override
  public int size() {
    return getItem() == null ? 0 : 1;
  }

  @Override
  public ItemT set(int index, ItemT t) {
    if (index != 0) throw new IndexOutOfBoundsException();
    ItemT oldValue = getItem();
    setItem(t);
    return oldValue;
  }

  @Override
  public void add(int index, ItemT item) {
    if (size() != 0) throw new IllegalStateException();
    setItem(item);
  }

  @Override
  public ItemT remove(int index) {
    if (index != 0) throw new IndexOutOfBoundsException();
    ItemT oldItem = getItem();
    if (oldItem == null) throw new IndexOutOfBoundsException();
    setItem(null);
    return oldItem;
  }
}