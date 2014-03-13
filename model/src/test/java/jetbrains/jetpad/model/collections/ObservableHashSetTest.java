/*
 * Copyright 2012-2014 JetBrains s.r.o
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
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

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
}