/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

public class UnmodifiableList<ElementT> extends AbstractList<ElementT> {
  private List<ElementT> myWrappedList;

  public UnmodifiableList(List<ElementT> wrappedList) {
    myWrappedList = wrappedList;
  }

  @Override
  public ElementT get(int index) {
    return myWrappedList.get(index);
  }

  @Override
  public int size() {
    return myWrappedList.size();
  }

  protected List<ElementT> getWrappedList() {
    return myWrappedList;
  }

  @Override
  public boolean add(ElementT elementT) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, ElementT element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ElementT remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends ElementT> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends ElementT> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
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
  public ElementT set(int index, ElementT element) {
    throw new UnsupportedOperationException();
  }
}