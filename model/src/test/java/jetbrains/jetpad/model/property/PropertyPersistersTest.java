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
package jetbrains.jetpad.model.property;


import jetbrains.jetpad.base.Persister;
import jetbrains.jetpad.base.Persisters;
import jetbrains.jetpad.base.function.Supplier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.base.Persisters.stringPersister;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertyPersistersTest {

  @Test
  public void nullStringValueProperty() {
    testNull(PropertyPersisters.valuePropertyPersister(stringPersister()));
  }

  @Test
  public void valuePropertyEmptyString() {
    assertNull(PropertyPersisters.valuePropertyPersister(stringPersister()).deserialize("").get());
  }

  @Test
  public void nullListOfStringProperties() {
    testNull(propListPersister);
  }

  @Test
  public void listOfStringProperties() {
    List<Property<String>> testList = new ArrayList<>();
    testList.add(new ValueProperty<>("hello"));
    testList.add(new ValueProperty<>(":world,!2312:fds,v;"));
    assertPropListEquals(testList, propListPersister.deserialize(propListPersister.serialize(testList)));
  }

  @Test
  public void listOfStringPropertiesWithNulls() {
    List<Property<String>> testList = new ArrayList<>();
    testList.add(new ValueProperty<String>(null));
    testList.add(null);
    assertPropListEquals(testList, propListPersister.deserialize(propListPersister.serialize(testList)));
  }


  private Persister<List<Property<String>>> propListPersister =
      Persisters.listPersister(PropertyPersisters.valuePropertyPersister(stringPersister()),
          new Supplier<List<Property<String>>>() {
            @Override
            public List<Property<String>> get() {
              return new ArrayList<>();
            }
          });

  private <T> void testNull(Persister<T> persister) {
    T defaultValue = persister.deserialize(null);
    assertEquals(defaultValue, persister.deserialize(persister.serialize(null)));
  }

  private void assertPropListEquals(List<Property<String>> expected, List<Property<String>> actual) {
    for (int i = 0; i < expected.size(); i++) {
      Property<String> left = expected.get(i);
      Property<String> right = actual.get(i);
      assertEquals((left == null), (right == null));
      if (left != null) {
        assertEquals(left.get(), right.get());
      }
    }
  }
}