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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ListSelectionTest {
  @Test
  public void nonListened() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);

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
  @Test
  public void listened() {
    Property<Boolean> src = new ValueProperty<>(false);
    final ObservableList<String> selected = new ObservableArrayList<>();
    ObservableList<String> res = testList(src, selected);
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
  @Test
  public void registrationRemoval() {
    final AtomicInteger propertyLsnrs = new AtomicInteger();
    final AtomicInteger collectionLsnrs = new AtomicInteger();
    Property<Boolean> src = listenersCountingProperty(propertyLsnrs);
    ObservableList<String> selected = listenersCountingList(collectionLsnrs);

    ObservableList<String> res = testList(src, selected);
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
    return Properties.selectList(
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

  private ObservableArrayList<String> listenersCountingList(final AtomicInteger collectionLsnrs) {
    return new ObservableArrayList<String>() {
      @Override
      public Registration addListener(CollectionListener<String> listener) {
        final Registration r = super.addListener(listener);
        collectionLsnrs.incrementAndGet();
        return new Registration() {
          @Override
          protected void doRemove() {
            r.remove();
            collectionLsnrs.decrementAndGet();
          }
        };
      }
    };
  }

  private ValueProperty<Boolean> listenersCountingProperty(final AtomicInteger propertyLsnrs) {
    return new ValueProperty<Boolean>(false) {
      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        final Registration r = super.addHandler(handler);
        propertyLsnrs.incrementAndGet();
        return new Registration() {
          @Override
          protected void doRemove() {
            r.remove();
            propertyLsnrs.decrementAndGet();
          }
        };
      }
    };
  }
}
