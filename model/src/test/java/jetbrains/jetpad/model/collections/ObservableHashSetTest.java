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

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    Mockito.verify(listener).onItemAdded(new CollectionItemEvent<>(null, "x", -1, CollectionItemEvent.EventType.ADD));
  }

  @Test
  public void remove() {
    set.add("x");
    Mockito.reset(listener);
    set.remove("x");
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>("x", null, -1, CollectionItemEvent.EventType.REMOVE));
  }

  @Test
  public void clear() {
    set.add("x");
    Mockito.reset(listener);
    set.clear();
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>("x", null, -1, CollectionItemEvent.EventType.REMOVE));
  }

  @Test
  public void nullValue() {
    set.add(null);
    Mockito.verify(listener).onItemAdded(new CollectionItemEvent<String>(null, null, -1, CollectionItemEvent.EventType.ADD));
    assertEquals(1, set.size());
    Mockito.reset(listener);
    set.remove(null);
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<String>(null, null, -1, CollectionItemEvent.EventType.REMOVE));
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
    Mockito.verify(listener).onItemRemoved(new CollectionItemEvent<>(toRemove, null, -1, CollectionItemEvent.EventType.REMOVE));
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

  @Test
  public void fireWhenNotAdded() {
    final Value<Boolean> afterCalled = new Value<>(false);
    set = new ObservableHashSet<String>() {
      @Override
      protected boolean doAdd(String item) {
        return false;
      }

      @Override
      protected void afterItemAdded(String item, boolean success) {
        assertEquals("x", item);
        assertFalse(success);
        afterCalled.set(true);
      }
    };
    set.addListener(listener);
    set.add("x");
    assertTrue(afterCalled.get());
    Mockito.verify(listener, Mockito.never()).onItemAdded(new CollectionItemEvent<>("x", null, -1, CollectionItemEvent.EventType.REMOVE));
  }

  @Test
  public void fireWhenNotRemoved() {
    final Value<Boolean> afterCalled = new Value<>(false);
    set = new ObservableHashSet<String>() {
      @Override
      protected boolean doRemove(String item) {
        return false;
      }

      @Override
      protected void afterItemRemoved(String item, boolean success) {
        assertEquals("x", item);
        assertFalse(success);
        afterCalled.set(true);
      }
    };
    set.add("x");
    set.addListener(listener);
    set.remove("x");
    assertTrue(afterCalled.get());
    Mockito.verify(listener, Mockito.never()).onItemRemoved(new CollectionItemEvent<>("x", null, -1, CollectionItemEvent.EventType.REMOVE));
  }
}