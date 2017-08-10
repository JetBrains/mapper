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
package jetbrains.jetpad.model.composite;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.model.composite.TestComposite.create;

public class UpperLowerFocusableTest {

  @Test
  public void upperFocusable() {
    TestComposite root = create(0, 0, 10, 2);

    TestComposite row1 = create(0, 0, 10, 1);
    TestComposite row2 = create(0, 1, 10, 1);

    TestComposite left = create(1, 0, 3, 1);
    TestComposite right = create(5, 0, 3, 1);

    row1.children().addAll(Arrays.asList(left, right));
    root.children().addAll(Arrays.asList(row1, row2));

    Assert.assertEquals(Arrays.asList(right, left), Lists.newArrayList(new CompositesWithBounds(0).upperFocusables(row2)));
  }

  @Test
  public void lowerFocusable() {
    TestComposite root = create(0, 0, 10, 2);

    TestComposite row1 = create(0, 0, 10, 1);
    TestComposite row2 = create(0, 1, 10, 1);

    TestComposite left = create(1, 1, 5, 1);
    TestComposite right = create(8, 1, 1, 1);

    row2.children().addAll(Arrays.asList(left, right));

    root.children().addAll(Arrays.asList(row1, row2));

    Assert.assertEquals(Arrays.asList(left, right), Lists.newArrayList(new CompositesWithBounds(0).lowerFocusables(row1)));
  }
}