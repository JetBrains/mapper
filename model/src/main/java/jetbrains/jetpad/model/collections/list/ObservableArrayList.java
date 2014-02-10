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

import java.util.ArrayList;
import java.util.List;

public class ObservableArrayList<ItemT> extends AbstractObservableList<ItemT> {
  private List<ItemT> myContainer;

  public ItemT get(int index) {
    if (myContainer == null) throw new ArrayIndexOutOfBoundsException(index);
    return myContainer.get(index);
  }

  public int size() {
    if (myContainer == null) {
      return 0;
    }
    return myContainer.size();
  }

  @Override
  public ItemT set(int index, ItemT t) {
    ItemT result = remove(index);
    add(index, t);
    return result;
  }

  @Override
  public void add(final int index, final ItemT item) {
    ensureContainerInitialized();

    add(index, item, new Runnable() {
      @Override
      public void run() {
        myContainer.add(index, item);
      }
    });
  }

  @Override
  public ItemT remove(final int index) {
    ensureContainerInitialized();

    ItemT result = myContainer.get(index);
    remove(index, result, new Runnable() {
      @Override
      public void run() {
        myContainer.remove(index);
        if (myContainer.isEmpty()) {
          myContainer = null;
        }
      }
    });
    return result;
  }

  private void ensureContainerInitialized() {
    if (myContainer == null) {
      myContainer = new ArrayList<>(1);
    }
  }
}