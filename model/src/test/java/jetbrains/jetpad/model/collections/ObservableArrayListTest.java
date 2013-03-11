/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObservableArrayListTest {
  private ObservableList<String> list = new ObservableArrayList<String>();
  private List<CollectionItemEvent<String>> events = new ArrayList<CollectionItemEvent<String>>();
  private CollectionListener<String> listener = new CollectionAdapter<String>() {
    @Override
    public void onItemAdded(CollectionItemEvent<String> event) {
      events.add(event);
    }

    @Override
    public void onItemRemoved(CollectionItemEvent<String> event) {
      events.add(event);
    }
  };

  @Before
  public void setUp() throws Exception {
    list.addListener(listener);
  }

  @Test
  public void itemAdd() {
    String item = "xyz";

    list.add(item);

    assertEvent(0, item, true);
  }
  
  @Test
  public void itemRemove() {
    final String item = "xyz";
    list.add(item);

    events.clear();
    
    list.remove(item);

    assertEvent(0, item, false);
  }
  
  @Test
  public void nonExistentItemRemove() {
    String item = "xyz";
    list.remove(item);

    assertEquals(0, events.size());
  }

  private void assertEvent(int index, String item, boolean added) {
    assertEquals(1, events.size());

    CollectionItemEvent<String> event = events.get(0);
    assertSame(item, event.getItem());
    assertEquals(index, event.getIndex());
    assertTrue(added == event.isAdded());
  }




}
