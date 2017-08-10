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

import java.util.ArrayList;
import java.util.List;

public class ObservableTreeList<ItemT> extends AbstractObservableList<ItemT> {
  private List<ItemT> myContainer;

  @Override
  public ItemT get(int index) {
    if (myContainer == null) {
      throw new ArrayIndexOutOfBoundsException(index);
    }

    return myContainer.get(index);
  }

  @Override
  public int size() {
    return myContainer == null ? 0 : myContainer.size();
  }

  @Override
  protected void doAdd(int index, ItemT item) {
    ensureContainerInitialized();
    myContainer.add(index, item);
  }

  @Override
  protected void doSet(int index, ItemT item) {
    myContainer.set(index, item);
  }

  @Override
  protected void doRemove(int index) {
    myContainer.remove(index);
    if (myContainer.isEmpty()) {
      myContainer = null;
    }
  }

  private void ensureContainerInitialized() {
    if (myContainer == null) {
      myContainer = new TreeList<>();
    }
  }
}