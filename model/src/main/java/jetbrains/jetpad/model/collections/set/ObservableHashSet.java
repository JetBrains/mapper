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
package jetbrains.jetpad.model.collections.set;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ObservableHashSet<ItemT> extends AbstractObservableSet<ItemT> {
  private Set<ItemT> mySet;

  @Override
  public Iterator<ItemT> iterator() {
    if (mySet == null) {
      return Collections.<ItemT>emptyList().iterator();
    }

    final Iterator<ItemT> iterator = mySet.iterator();
    return new Iterator<ItemT>() {
      private ItemT myLastReturned;

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public ItemT next() {
        myLastReturned = iterator.next();
        return myLastReturned;
      }

      @Override
      public void remove() {
        ObservableHashSet.this.remove(myLastReturned, new Runnable() {
          @Override
          public void run() {
            iterator.remove();
          }
        });
        myLastReturned = null;
      }
    };
  }

  @Override
  public int size() {
    if (mySet == null) return 0;
    return mySet.size();
  }

  @Override
  public boolean contains(Object o) {
    if (mySet == null) return false;
    return mySet.contains(o);
  }

  @Override
  public boolean add(final ItemT t) {
    ensureSetInitialized();
    if (mySet.contains(t)) return false;
    add(t, new Runnable() {
      @Override
      public void run() {
        mySet.add(t);
      }
    });
    return true;
  }

  @Override
  public boolean remove(final Object o) {
    if (mySet == null) return false;
    if (!mySet.contains(o)) return false;
    remove((ItemT) o, new Runnable() {
      @Override
      public void run() {
        ensureSetInitialized();
        mySet.remove((ItemT) o);
      }
    });
    return true;
  }

  private void ensureSetInitialized() {
    if (mySet == null) {
      mySet = new HashSet<ItemT>(1);
    }
  }
}