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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObservableHashSetTest {
  private ObservableSet<String> set = new ObservableHashSet<>();
  private CollectionListener<String> listener = Mockito.mock(CollectionAdapter.class);

  @Before
  public void init() {
    set.addListener(listener);
  }

  @Test
  public void add() {
    set.add("x");
    Mockito.verify(listener).onItemAdded(new CollectionItemEvent<>("x", -1, true));
  }

  @Test
  public void remove() {
    set.add("x");
    Mockito.reset(listener);
    set.remove("x");
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>("x", -1, false));
  }

  @Test
  public void clear() {
    set.add("x");
    Mockito.reset(listener);
    set.clear();
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>("x", -1, false));
  }

  @Test
  public void nullValue() {
    set.add(null);
    Mockito.verify(listener).onItemAdded(new CollectionItemEvent<String>(null, -1, true));
    assertEquals(1, set.size());
    Mockito.reset(listener);
    set.remove(null);
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<String>(null, -1, false));
    assertTrue(set.isEmpty());
  }

  @Test
  public void iterator() {
    set.add("x");
    set.add("y");
    Mockito.reset(listener);
    Iterator<String> i = set.iterator();
    String toRemove = i.next();
    i.remove();
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>(toRemove, -1, false));
    assertEquals(1, set.size());
  }

  @Test(expected = IllegalStateException.class)
   public void duplicateIteratorRemove() {
    Value<Integer> counter = createSetCountingBeforeRemove();
    set.add("x");
    set.add(null);
    assertEquals(2, set.size());
    Iterator<String> i = set.iterator();
    i.next();
    i.remove();
    counter.set(0);
    i.remove();
    try {
      i.remove();
    } finally {
      assertEquals(0, (int) counter.get());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void iteratorRemoveBeforeNext() {
    Value<Integer> counter = createSetCountingBeforeRemove();
    set.add("x");
    Iterator<String> i = set.iterator();
    try {
      i.remove();
    } finally {
      assertEquals(0, (int) counter.get());
    }
  }

  private Value<Integer> createSetCountingBeforeRemove() {
    final Value<Integer> counter = new Value<>(0);
    set = new ObservableHashSet<String>() {
      @Override
      protected void checkRemove(String item) {
        counter.set(counter.get() + 1);
      }

      @Override
      protected void beforeItemRemoved(String item) {
        counter.set(counter.get() + 1);
      }
    };
    set.addListener(listener);
    return counter;
  }
}