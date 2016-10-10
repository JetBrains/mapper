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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SortByConstantTest {
  ObservableList<String> items = new ObservableArrayList<>();
  ObservableList<String> target = new ObservableArrayList<>();

  Registration myReg;

  @Before
  public void setup() {
    Transformer<ObservableList<String>, ObservableList<String>> trans = Transformers.sortByConstant(x -> x, String::compareTo);
    Transformation<ObservableList<String>, ObservableList<String>> transformation = trans.transform(items, target);
    myReg = new Registration() {
      @Override
      protected void doRemove() {
        transformation.dispose();
      }
    };
  }

  @Test
  public void simpleAdds() {
    items.add("a");
    items.add("z");
    items.add("x");

    assertEquals(Arrays.asList("a", "x", "z"), target);
  }

  @Test
  public void simpleRemoves() {
    items.add("a");
    items.add("z");
    items.add("x");

    items.remove("x");

    assertEquals(Arrays.asList("a", "z"), target);
  }

  @Test
  public void dipose() {
    items.add("a");
    items.add("z");

    myReg.remove();

    assertTrue(target.isEmpty());
  }
}