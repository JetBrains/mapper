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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;

import java.util.Collection;
import java.util.Iterator;

public class UnmodifiableObservableCollection<ItemT> implements ObservableCollection<ItemT> {
  private final ObservableCollection<ItemT> myWrappedCollection;

  public UnmodifiableObservableCollection(ObservableCollection<ItemT> wrappedCollection) {
    myWrappedCollection = wrappedCollection;
  }

  @Override
  public Registration addListener(CollectionListener<ItemT> l) {
    return myWrappedCollection.addListener(l);
  }

  @Override
  public Registration addHandler(EventHandler<? super CollectionItemEvent<? extends ItemT>> handler) {
    return myWrappedCollection.addHandler(handler);
  }

  @Override
  public int size() {
    return myWrappedCollection.size();
  }

  @Override
  public boolean isEmpty() {
    return myWrappedCollection.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return myWrappedCollection.contains(o);
  }

  @Override
  public Iterator<ItemT> iterator() {
    return myWrappedCollection.iterator();
  }

  @Override
  public Object[] toArray() {
    return myWrappedCollection.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return myWrappedCollection.toArray(a);
  }

  @Override
  public boolean add(ItemT itemT) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return myWrappedCollection.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends ItemT> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}