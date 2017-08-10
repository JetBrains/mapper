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

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FlattenPropertyListTest {
  private ObservableList<Property<String>> list = new ObservableArrayList<>();
  private ObservableList<String> flattenedList = new ObservableArrayList<>();
  private Transformation<ObservableList<Property<String>>, ObservableList<String>> trans;

  @Before
  public void init() {
    list.addAll(Arrays.asList(new ValueProperty<>("a"), new ValueProperty<>("b"), new ValueProperty<>("c")));
    trans = Transformers.<String, Property<String>>flattenPropertyList().transform(list, flattenedList);
  }

  @Test
  public void initialState() {
    assertList("a", "b", "c");
  }

  @Test
  public void propSet() {
    list.get(1).set("z");

    assertList("a", "z", "c");
  }

  @Test
  public void itemAdd() {
    list.add(1, new ValueProperty<>("x"));

    assertList("a", "x", "b", "c");
  }

  @Test
  public void itemRemove() {
    list.remove(1);

    assertList("a", "c");
  }

  @Test
  public void dispose() {
    trans.dispose();

    assertList();
  }

  private void assertList(String... vals) {
    assertEquals(Arrays.asList(vals), flattenedList);
  }
}