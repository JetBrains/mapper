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
package jetbrains.jetpad.model.collections.set;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ObservableHashSet<ItemT> extends AbstractObservableSet<ItemT> {
  private Set<ItemT> mySet;

  @Override
  public int size() {
    return mySet == null ? 0 : mySet.size();
  }

  @Override
  public boolean contains(Object o) {
    return mySet == null ? false : mySet.contains(o);
  }

  @Override
  protected boolean doAdd(ItemT item) {
    ensureSetInitialized();
    return mySet.add(item);
  }

  @Override
  protected boolean doRemove(ItemT item) {
    return mySet.remove(item);
  }

  @Override
  protected Iterator<ItemT> getIterator() {
    return mySet.iterator();
  }

  private void ensureSetInitialized() {
    if (mySet == null) {
      mySet = new HashSet<>(1);
    }
  }
}