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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReverseTest {
  private ObservableList<String> from = new ObservableArrayList<>();
  private ObservableList<String> to = new ObservableArrayList<>();
  private Transformer<ObservableList<String>, ObservableList<String>> reverse = Transformers.reverse();
  private Transformation<ObservableList<String>, ObservableList<String>> transformation;

  @After
  public void cleanup() {
    transformation.dispose();
  }

  @Test
  public void emptySource() {
    transformation = reverse.transform(from, to);
    assertEquals(from, to);
  }

  @Test
  public void singleItem() {
    from.add("a");
    transformation = reverse.transform(from, to);
    assertEquals(from, to);
  }

  @Test
  public void addItems() {
    transformation = reverse.transform(from, to);
    from.add("a");
    from.add("b");
    assertEquals(reverseFrom(), to);
  }

  @Test
  public void removeItems() {
    from.add("a");
    from.add("b");
    transformation = reverse.transform(from, to);
    from.remove("a");
    assertEquals(from, to);
    from.remove("b");
    assertTrue(from.isEmpty());
    assertEquals(from, to);
  }

  @Test
  public void setItems() {
    from.add("a");
    from.add("b");
    transformation = reverse.transform(from, to);
    from.set(0, "c");
    assertEquals("c", from.get(0));
    assertEquals(reverseFrom(), to);

    from.set(1, "d");
    assertEquals("d", from.get(1));
    assertEquals(reverseFrom(), to);
  }

  private List<String> reverseFrom() {
    List<String> source = new ArrayList<>(from);
    Collections.reverse(source);
    return source;
  }
}