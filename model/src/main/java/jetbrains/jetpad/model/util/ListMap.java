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
package jetbrains.jetpad.model.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Memory efficient implementation of a map based on an array.
 * <p>
 * It works better than a HashMap and TreeMap on small sized collections.
 */
public class ListMap<K, V> {
  private static final Object[] EMPTY_ARRAY = new Object[0];

  private Object[] myData = EMPTY_ARRAY;

  public ListMap() {
  }

  public boolean containsKey(K key) {
    return findByKey(key) >= 0;
  }

  public V remove(K key) {
    int index = findByKey(key);
    if (index >= 0) {
      @SuppressWarnings("unchecked")
      V value = (V) myData[index + 1];
      removeAt(index);
      return value;
    } else {
      return null;
    }
  }

  public Set<K> keySet() {
    return new AbstractSet<K>() {
      @Override
      public Iterator<K> iterator() {
        return mapIterator(new IteratorSpec() {
          @Override
          public Object get(int index) {
            return myData[index];
          }
        });
      }

      @Override
      public int size() {
        return ListMap.this.size();
      }
    };
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public Collection<V> values() {
    return new AbstractCollection<V>() {
      @Override
      public Iterator<V> iterator() {
        return mapIterator(new IteratorSpec() {
          @Override
          public Object get(int index) {
            return myData[index + 1];
          }
        });
      }

      @Override
      public int size() {
        return ListMap.this.size();
      }
    };
  }

  public Set<Entry> entrySet() {
    return new AbstractSet<Entry>() {
      @Override
      public Iterator<Entry> iterator() {
        return mapIterator(new IteratorSpec() {
          @Override
          public Object get(int index) {
            return new Entry(index);
          }
        });
      }

      @Override
      public int size() {
        return ListMap.this.size();
      }
    };
  }

  public int size() {
    return myData.length / 2;
  }

  public V put(K key, V value) {
    int index = findByKey(key);
    if (index >= 0) {
      @SuppressWarnings("unchecked")
      V oldValue = (V) myData[index + 1];
      myData[index + 1] = value;
      return oldValue;
    }
    Object[] newArray = new Object[myData.length + 2];
    System.arraycopy(myData, 0, newArray, 0, myData.length);
    newArray[myData.length] = key;
    newArray[myData.length + 1] = value;
    myData = newArray;
    return null;
  }

  public V get(K key) {
    int index = findByKey(key);
    if (index == -1) {
      return null;
    }
    @SuppressWarnings("unchecked")
    V result = (V) myData[index + 1];
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("{");
    for (int i = 0; i < myData.length; i += 2) {
      @SuppressWarnings("unchecked")
      K k = (K) myData[i];
      @SuppressWarnings("unchecked")
      V v = (V) myData[i + 1];
      if (i != 0) {
        builder.append(",");
      }
      builder.append(k).append("=").append(v);
    }
    builder.append("}");

    return builder.toString();
  }

  private <T> Iterator<T> mapIterator(final IteratorSpec spec) {
    return new Iterator<T>() {
      private int index = 0;
      private boolean nextCalled = false;

      @Override
      public boolean hasNext() {
        return index < myData.length;
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        nextCalled = true;
        @SuppressWarnings("unchecked")
        T value = (T) spec.get(index);
        index += 2;
        return value;
      }

      @Override
      public void remove() {
        if (!nextCalled) {
          throw new IllegalStateException();
        }
        index -= 2;
        removeAt(index);
        nextCalled = false;
      }
    };
  }

  private int findByKey(K key) {
    for (int i = 0; i < myData.length; i += 2) {
      @SuppressWarnings("unchecked")
      K k = (K) myData[i];
      if (Objects.equals(key, k)) {
        return i;
      }
    }
    return -1;
  }

  private void removeAt(int index) {
    if (myData.length == 2) {
      myData = EMPTY_ARRAY;
      return;
    }
    Object[] newArray = new Object[myData.length - 2];
    System.arraycopy(myData, 0, newArray, 0, index);
    System.arraycopy(myData, index + 2, newArray, index, myData.length - index - 2);
    myData = newArray;
  }

  public class Entry {
    private final int myIndex;

    private Entry(int index) {
      myIndex = index;
    }

    public K key() {
      @SuppressWarnings("unchecked")
      K result = (K) myData[myIndex];
      return result;
    }

    public V value() {
      @SuppressWarnings("unchecked")
      V result = (V) myData[myIndex + 1];
      return result;
    }
  }

  private interface IteratorSpec {
    Object get(int index);
  }
}
