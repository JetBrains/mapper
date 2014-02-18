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
package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelectListTest {
  ObservableList<Integer> from;
  ObservableList<String> to;
  Transformer<ObservableList<Integer>, ObservableList<String>> select;

  @Before
  public void setup() {
    from = new ObservableArrayList<>();
    to = new ObservableArrayList<>();
    select = Transformers.selectList(new Function<Integer, String>() {
      @Override
      public String apply(Integer input) {
        return Integer.toString(input);
      }
    });
  }

  @Test
  public void notEmptyFrom() {
    from.add(0);
    select.transform(from, to);

    assertTrue(to.size() == 1);
    assertTrue(to.contains("0"));
  }

  @Test
  public void addItem() {
    select.transform(from, to);
    from.add(0);

    assertTrue(to.size() == 1);
    assertTrue(to.contains("0"));
  }

  @Test
  public void removeItem() {
    from.add(0);
    select.transform(from, to);
    from.remove(0);

    assertTrue(to.size() == 0);
  }

  @Test
  public void disposeTransformation() {
    Transformation<ObservableList<Integer>, ObservableList<String>> transformation = select.transform(from, to);
    transformation.dispose();
    from.add(0);

    assertTrue(to.size() == 0);
  }

  @Test
  public void rightOrder() {
    from.add(0);
    select.transform(from, to);

    from.add(1, 1);
    assertTrue(to.size() == 2);
    assertEquals("0", to.get(0));
    assertEquals("1", to.get(1));

    from.add(0, 2);
    assertTrue(to.size() == 3);
    assertEquals("2", to.get(0));
  }
}