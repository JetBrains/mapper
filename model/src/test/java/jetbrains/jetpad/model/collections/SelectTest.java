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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableCollections;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.ADD;
import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.REMOVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SelectTest {
  public static final int TEST_LIST_SIZE = 3;

  @Test
  public void nonListenedList() {
    Property<Boolean> src = new ValueProperty<>(false);
    ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);

    testNonListened(src, selected, res);
  }

  @Test
  public void nonListenedCollection() {
    Property<Boolean> src = new ValueProperty<>(false);
    ObservableCollection<String> selected = new ObservableHashSet<>();
    ObservableCollection<String> res = testCollection(src, selected);

    testNonListened(src, selected, res);
  }

  @Test
  public void listenedList() {
    Property<Boolean> src = new ValueProperty<>(false);
    ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);

    testListened(src, selected, res);
  }

  @Test
  public void listenedCollection() {
    Property<Boolean> src = new ValueProperty<>(false);
    ObservableCollection<String> selected = new ObservableHashSet<>();
    ObservableCollection<String> res = testCollection(src, selected);

    testListened(src, selected, res);
  }

  @Test
  public void listRegistrations() {
    AtomicInteger pc = new AtomicInteger();
    AtomicInteger cc = new AtomicInteger();
    Property<Boolean> src = listenersCountingProperty(pc);
    ObservableList<String> selected = listenersCountingList(cc);

    ObservableList<String> res = testList(src, selected);

    testRegistrations(src, res, pc, cc);
  }

  @Test
  public void collectionRegistrations() {
    AtomicInteger propertyLsnrs = new AtomicInteger();
    AtomicInteger collectionLsnrs = new AtomicInteger();
    Property<Boolean> src = listenersCountingProperty(propertyLsnrs);
    ObservableList<String> selected = listenersCountingList(collectionLsnrs);

    ObservableCollection<String> res = testCollection(src, selected);

    testRegistrations(src, res, propertyLsnrs, collectionLsnrs);
  }

  @Test
  public void listInnerUnfollow() {
    AtomicInteger propertyLsnrs = new AtomicInteger();
    AtomicInteger collectionLsnrs = new AtomicInteger();
    final Property<Boolean> src = listenersCountingProperty(propertyLsnrs);
    ObservableList<String> selected = listenersCountingList(collectionLsnrs);

    ObservableList<String> res = testList(src, selected);

    final Registration r1 = res.addListener(new CollectionAdapter<String>());
    final AtomicInteger handlerPass = new AtomicInteger();
    src.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        handlerPass.incrementAndGet();
        if (handlerPass.get() == 1) {
          r1.remove();
        }
        src.set(false);
      }
    });
    src.set(true);
  }

  @Test
  public void collectionNonEmpty() {
    ObservableCollection<String> res = testCollection(Properties.constant(true), newTestList());

    res.addListener(new CollectionAdapter<String>());
    assertEquals(TEST_LIST_SIZE, res.size());
  }

  @Test
  public void listNonEmpty() {
    ObservableList<String> res = testList(Properties.constant(true), newTestList());

    res.addListener(new CollectionAdapter<String>());
    assertEquals(TEST_LIST_SIZE, res.size());
  }

  @Test
  public void listNonEmptyIterator() {
    ObservableList<String> res = testList(Properties.constant(true), newTestList());

    assertEquals(TEST_LIST_SIZE, res.size());
    for (String s : res) {
      assertNotNull(s);
    }

    res.addListener(new CollectionAdapter<String>());
    assertEquals(TEST_LIST_SIZE, res.size());
    for (String s : res) {
      assertNotNull(s);
    }
  }

  @Test
  public void collectionNonEmptyIterator() {
    ObservableCollection<String> res = testCollection(Properties.constant(true), newTestList());

    assertEquals(TEST_LIST_SIZE, res.size());
    for (String s : res) {
      assertNotNull(s);
    }

    res.addListener(new CollectionAdapter<String>());
    assertEquals(TEST_LIST_SIZE, res.size());
    for (String s : res) {
      assertNotNull(s);
    }
  }

  private ObservableList<String> newTestList() {
    ObservableList<String> test = new ObservableArrayList<>();
    for (int i = 0; i < TEST_LIST_SIZE; i++) {
      test.add(String.valueOf(i));
    }
    return test;
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

  private ObservableList<String> testList(ReadableProperty<Boolean> src, final ObservableList<String> selected) {
    return ObservableCollections.selectList(
      src,
        new Function<Boolean, ObservableList<String>>() {
          @Override
          public ObservableList<String> apply(Boolean source) {
            return source ? selected : null;
          }
        });
  }

  private ObservableCollection<String> testCollection(ReadableProperty<Boolean> src, final ObservableCollection<String> selected) {
    return ObservableCollections.selectCollection(
      src,
        new Function<Boolean, ObservableCollection<String>>() {
          @Override
          public ObservableCollection<String> apply(Boolean source) {
            return source ? selected : null;
          }
        });
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