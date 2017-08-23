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

import jetbrains.jetpad.model.collections.CollectionItemEvent.EventType;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.SET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObservableArrayListTest {
  private ObservableList<String> list = new ObservableArrayList<>();
  private List<CollectionItemEvent<? extends String>> events = new ArrayList<>();
  private CollectionListener<String> listener = new CollectionAdapter<String>() {
    @Override
    public void onItemAdded(CollectionItemEvent<? extends String> event) {
      events.add(event);
    }

    @Override
    public void onItemRemoved(CollectionItemEvent<? extends String> event) {
      events.add(event);
    }
  };

  @Before
  public void setUp() {
    list.addListener(listener);
  }

  @Test
  public void itemAdd() {
    String item = "xyz";

    list.add(item);

    assertEvent(0, null, item, EventType.ADD);
  }

  @Test
  public void itemRemove() {
    final String item = addSampleItem();

    list.remove(item);

    assertEvent(0, item, null, EventType.REMOVE);
  }

  @Test
  public void nonExistentItemRemove() {
    String item = "xyz";
    list.remove(item);

    assertEquals(0, events.size());
  }

  @Test
  public void addAtOkIndex() {
    String item = "xyz";
    list.add(0, item);
    assertEvent(0, null, item, EventType.ADD);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void addAtInvalidIndex() {
    try {
      list.add(1, "xyz");
    } finally {
      assertTrue(events.isEmpty());
      assertTrue(list.isEmpty());
    }
  }

  @Test
  public void removeAtOkIndex() {
    String item = addSampleItem();
    String removed = list.remove(0);
    assertEquals(item, removed);
    assertEvent(0, item, null, EventType.REMOVE);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void removeAtInvalidIndex() {
    String item = addSampleItem();
    try {
      String removed = list.remove(1);
      assertEquals(item, removed);
    } finally {
      assertTrue(events.isEmpty());
      assertEquals(1, list.size());
    }
  }

  @Test
  public void iteratorRemove() {
    addSampleItem();
    Iterator<String> i = list.iterator();
    i.next();
    i.remove();
    assertEvent(0, "xyz", null, EventType.REMOVE);
  }

  @Test
  public void oneHandlerEventOnSet() {
    addSampleItem();
    RecordingCollectionEventHandler<String> counter = new RecordingCollectionEventHandler<>();
    list.addHandler(counter);
    list.set(0, "abc");
    assertEquals(1, counter.getCounter());
    assertEquals(SET, counter.getEvents().get(0).getType());
  }

  private String addSampleItem() {
    String item = "xyz";
    list.add(item);
    events.clear();
    return item;
  }

  private void assertEvent(int index, String oldItem, String newItem, EventType type) {
    assertEquals(1, events.size());

    CollectionItemEvent<? extends String> event = events.get(0);
    assertEquals(new CollectionItemEvent<>(oldItem, newItem, index, type), event);
  }
}