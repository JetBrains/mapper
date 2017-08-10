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
package jetbrains.jetpad.model.collections.wrappers;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import static jetbrains.jetpad.model.collections.wrappers.Events.wrapEvent;

public class ObservableSetWrapper<SourceItemT, TargetItemT> implements ObservableSet<TargetItemT> {
  private final ObservableSet<SourceItemT> mySource;
  private final Function<SourceItemT, TargetItemT> myStoT;
  private final Function<TargetItemT, SourceItemT> myTtoS;

  public ObservableSetWrapper(ObservableSet<SourceItemT> source, Function<SourceItemT, TargetItemT> toTarget, Function<TargetItemT, SourceItemT> toSource) {
    mySource = source;
    myStoT = toTarget;
    myTtoS = toSource;
  }

  @Override
  public Registration addListener(final CollectionListener<TargetItemT> l) {
    return mySource.addListener(new CollectionListener<SourceItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends SourceItemT> event) {
        l.onItemAdded(wrapEvent(event, myStoT));
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends SourceItemT> event) {
        l.onItemSet(wrapEvent(event, myStoT));
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends SourceItemT> event) {
        l.onItemRemoved(wrapEvent(event, myStoT));
      }
    });
  }

  @Override
  public Registration addHandler(final EventHandler<? super CollectionItemEvent<? extends TargetItemT>> handler) {
    return mySource.addHandler(new EventHandler<CollectionItemEvent<? extends SourceItemT>>() {
      @Override
      public void onEvent(CollectionItemEvent<? extends SourceItemT> event) {
        handler.onEvent(wrapEvent(event, myStoT));
      }
    });
  }

  @Override
  public int size() {
    return mySource.size();
  }

  @Override
  public boolean isEmpty() {
    return mySource.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    if (o == null) return false;
    return mySource.contains(myTtoS.apply((TargetItemT) o));
  }

  @Override
  public Iterator<TargetItemT> iterator() {
    return new Iterator<TargetItemT>() {
      private Iterator<SourceItemT> myIterator = mySource.iterator();

      @Override
      public boolean hasNext() {
        return myIterator.hasNext();
      }

      @Override
      public TargetItemT next() {
        return myStoT.apply(myIterator.next());
      }

      @Override
      public void remove() {
        myIterator.remove();
      }
    };
  }

  @Override
  public Object[] toArray() {
    Object[] result = new Object[mySource.size()];
    int current = 0;
    for (SourceItemT item : mySource) {
      result[current++] = myStoT.apply(item);
    }
    return result;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return new HashSet<>(this).toArray(a);
  }

  @Override
  public boolean add(TargetItemT wrapper) {
    return mySource.add(myTtoS.apply(wrapper));
  }

  @Override
  public boolean remove(Object o) {
    return mySource.remove(myTtoS.apply((TargetItemT) o));
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c) {
      if (!mySource.contains(myTtoS.apply((TargetItemT) o))) return false;
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends TargetItemT> c) {
    boolean changed = false;
    for (TargetItemT w : c) {
      if (add(w)) {
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    Iterator<TargetItemT> it = iterator();
    while (it.hasNext()) {
      TargetItemT current = it.next();
      if (!c.contains(current)) {
        it.remove();
      }
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object o : c) {
      if (mySource.remove(myTtoS.apply((TargetItemT) o))) {
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public void clear() {
    mySource.clear();
  }
}