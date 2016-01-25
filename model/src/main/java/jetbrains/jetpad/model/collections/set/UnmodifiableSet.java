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
package jetbrains.jetpad.model.collections.set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class UnmodifiableSet<ElementT> extends AbstractSet<ElementT> {
  private Set<ElementT> myWrappedSet;

  public UnmodifiableSet(Set<ElementT> wrappedSet) {
    myWrappedSet = wrappedSet;
  }

  protected Set<ElementT> getWrappedSet() {
    return myWrappedSet;
  }

  @Override
  public Iterator<ElementT> iterator() {
    final Iterator<ElementT> it = myWrappedSet.iterator();
    return new Iterator<ElementT>() {
      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public ElementT next() {
        return it.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public int size() {
    return myWrappedSet.size();
  }

  @Override
  public boolean contains(Object o) {
    return myWrappedSet.contains(o);
  }

  @Override
  public Object[] toArray() {
    return myWrappedSet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return myWrappedSet.toArray(a);
  }

  @Override
  public boolean add(ElementT entity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends ElementT> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object o) {
    return myWrappedSet.equals(o);
  }

  @Override
  public int hashCode() {
    return myWrappedSet.hashCode();
  }
}