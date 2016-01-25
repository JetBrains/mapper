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
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableCollections;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.Selector;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SelectTest {
  @Test
  public void nonListenedList() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);

    testNonListened(src, selected, res);
  }

  @Test
  public void nonListenedCollection() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableCollection<String> selected = new ObservableHashSet<>();
    ObservableCollection<String> res = testCollection(src, selected);

    testNonListened(src, selected, res);
  }

  @Test
  public void listenedList() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);

    testListened(src, selected, res);
  }

  @Test
  public void listenedCollection() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableCollection<String> selected = new ObservableHashSet<>();
    ObservableCollection<String> res = testCollection(src, selected);

    testListened(src, selected, res);
  }

  @Test
  public void listRegistrations() {
    final AtomicInteger pc = new AtomicInteger();
    final AtomicInteger cc = new AtomicInteger();
    Property<Boolean> src = listenersCountingProperty(pc);
    ObservableList<String> selected = listenersCountingList(cc);

    ObservableList<String> res = testList(src, selected);

    testRegistrations(src, res, pc, cc);
  }

  @Test
  public void collectionRegistrations() {
    final AtomicInteger propertyLsnrs = new AtomicInteger();
    final AtomicInteger collectionLsnrs = new AtomicInteger();
    Property<Boolean> src = listenersCountingProperty(propertyLsnrs);
    ObservableList<String> selected = listenersCountingList(collectionLsnrs);

    ObservableCollection<String> res = testCollection(src, selected);

    testRegistrations(src, res, propertyLsnrs, collectionLsnrs);
  }

  private void testNonListened(Property<Boolean> src, ObservableCollection<String> selected, ObservableCollection<String> res) {
    assertEquals(0, res.size());

    selected.add("1");
    assertEquals(0, res.size());

    src.set(true);
    assertEquals(selected.size(), res.size());

    selected.add("2");
    assertEquals(selected.size(), res.size());

    src.set(false);
    assertEquals(0, res.size());
  }

  @SuppressWarnings("unchecked")
  private void testListened(Property<Boolean> src, ObservableCollection<String> selected, ObservableCollection<String> res) {
    CollectionListener<String> mock = mock(CollectionListener.class);
    res.addListener(mock);

    selected.add("1");
    verifyZeroInteractions(mock);
    assertEquals(0, res.size());

    src.set(true);
    verify(mock).onItemAdded(new CollectionItemEvent<>(null, "1", 0, ADD));
    assertEquals(selected.size(), res.size());

    selected.add("2");
    verify(mock).onItemAdded(new CollectionItemEvent<>(null, "2", 1, ADD));
    assertEquals(selected.size(), res.size());

    src.set(false);
    verify(mock).onItemRemoved(new CollectionItemEvent<>("1", null, 0, REMOVE));
    verify(mock).onItemRemoved(new CollectionItemEvent<>("2", null, 0, REMOVE));
    assertEquals(0, res.size());
  }

  @SuppressWarnings("unchecked")
  private void testRegistrations(Property<Boolean> src, ObservableCollection<String> res, AtomicInteger propertyLsnrs, AtomicInteger collectionLsnrs) {
    assertEquals(0, propertyLsnrs.get());
    assertEquals(0, collectionLsnrs.get());

    CollectionListener<String> mock = mock(CollectionListener.class);
    Registration mockReg = res.addListener(mock);
    assertEquals(1, propertyLsnrs.get());
    assertEquals(0, collectionLsnrs.get());

    src.set(true);
    assertEquals(1, propertyLsnrs.get());
    assertEquals(1, collectionLsnrs.get());

    src.set(false);
    assertEquals(1, propertyLsnrs.get());
    assertEquals(0, collectionLsnrs.get());

    src.set(true);
    assertEquals(1, propertyLsnrs.get());
    assertEquals(1, collectionLsnrs.get());
    mockReg.remove();
    assertEquals(0, propertyLsnrs.get());
    assertEquals(0, collectionLsnrs.get());
  }

  private ObservableList<String> testList(Property<Boolean> src, final ObservableList<String> selected) {
    return ObservableCollections.selectList(
      src,
      new Selector<Boolean, ObservableList<String>>() {
        @Override
        public ObservableList<String> select(Boolean source) {
          if (source) {
            return selected;
          } else {
            return null;
          }
        }
      }
    );
  }

  private ObservableCollection<String> testCollection(Property<Boolean> src, final ObservableCollection<String> selected) {
    return ObservableCollections.selectCollection(
      src,
      new Selector<Boolean, ObservableCollection<String>>() {
        @Override
        public ObservableCollection<String> select(Boolean source) {
          if (source) {
            return selected;
          } else {
            return null;
          }
        }
      }
    );
  }

  private ObservableList<String> listenersCountingList(final AtomicInteger counter) {
    return new ObservableArrayList<String>() {
      @Override
      public Registration addListener(CollectionListener<String> listener) {
        final Registration r = super.addListener(listener);
        counter.incrementAndGet();
        return new Registration() {
          @Override
          protected void doRemove() {
            r.remove();
            counter.decrementAndGet();
          }
        };
      }
    };
  }

  private Property<Boolean> listenersCountingProperty(final AtomicInteger counter) {
    return new ValueProperty<Boolean>(false) {
      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        final Registration r = super.addHandler(handler);
        counter.incrementAndGet();
        return new Registration() {
          @Override
          protected void doRemove() {
            r.remove();
            counter.decrementAndGet();
          }
        };
      }
    };
  }
}