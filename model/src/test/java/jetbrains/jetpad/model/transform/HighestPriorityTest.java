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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HighestPriorityTest {
  private ObservableCollection<String> from;
  private Transformation<ObservableCollection<String>, ObservableCollection<String>> trans;
  private ObservableCollection<String> to;

  @Before
  public void init() {
    from = new ObservableArrayList<>();
    from.addAll(Arrays.asList("a", "b", "cc"));
    trans = Transformers.highestPriority(new Function<String, Integer>() {
      @Override
      public Integer apply(String value) {
        return value.length();
      }
    }).transform(from);
    to = trans.getTarget();
  }

  @Test
  public void initialState() {
    assertItems("cc");
  }

  @Test
  public void addLowerPriority() {
    from.add("c");
    assertItems("cc");
  }

  @Test
  public void addHighPriority() {
    from.add("dd");
    assertItems("cc", "dd");
  }

  @Test
  public void addHigherPriority() {
    from.add("dddd");
    assertItems("dddd");
  }

  @Test
  public void addDifferentPriorities() {
    from.addAll(Arrays.asList("d", "dd"));
    assertItems("cc", "dd");
  }

  @Test
  public void removeLowerPriority() {
    from.remove("a");
    assertItems("cc");
  }

  @Test
  public void removeHighPriority() {
    from.remove("cc");
    assertItems("a", "b");
  }

  @Test
  public void addRemove() {
    from.add("dddd");
    from.remove("dddd");
    assertItems("cc");
    from.remove("cc");
    assertItems("a", "b");
  }

  @Test
  public void clear() {
    from.removeAll(Arrays.asList("a", "b", "cc"));
    assertItems();
  }

  @Test
  public void dispose() {
    trans.dispose();
    from.add("dddd");
    assertItems("cc");
    from.remove("cc");
    assertItems("cc");
  }

  private void assertItems(String ... items) {
    assertEquals(items.length, to.size());
    assertTrue(to.containsAll(Arrays.asList(items)));
  }
}