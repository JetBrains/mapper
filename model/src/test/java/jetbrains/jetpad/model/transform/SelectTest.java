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
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelectTest {
  private ObservableCollection<Integer> from;
  private ObservableCollection<String> to;
  private Transformer<ObservableCollection<Integer>, ObservableCollection<String>> select;

  @Before
  public void setup() {
    from = new ObservableHashSet<>();
    to = new ObservableHashSet<>();
    select = Transformers.select(new Function<Integer, String>() {
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

    assertEquals(1, to.size());
    assertTrue(to.contains("0"));
  }

  @Test
  public void addItem() {
    select.transform(from, to);
    from.add(0);

    assertEquals(1, to.size());
    assertTrue(to.contains("0"));
  }

  @Test
  public void removeItem() {
    from.add(0);
    select.transform(from, to);
    from.remove(0);

    assertTrue(to.isEmpty());
  }

  @Test
  public void disposeTransformation() {
    Transformation<ObservableCollection<Integer>, ObservableCollection<String>> transformation = select.transform(from, to);
    transformation.dispose();
    from.add(0);

    assertTrue(to.isEmpty());
  }
}