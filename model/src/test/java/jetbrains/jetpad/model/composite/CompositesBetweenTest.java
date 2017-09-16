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

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c11;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c12;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c2;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c21;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c31;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c311;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c312;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c313;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3131;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3132;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3133;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c32;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c331;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c332;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3331;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3332;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3333;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3334;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3335;
import static org.junit.Assert.assertEquals;

public class CompositesBetweenTest extends BaseTestCase {

  @Test
  public void same() {
    SimpleComposite root = new SimpleComposite("root");
    assertBetween(root, root, Collections.<SimpleComposite>emptyList());
    assertBetween(c21, c21, Collections.<SimpleComposite>emptyList());
  }

  @Test(expected=IllegalArgumentException.class)
  public void nonExisting() {
    Composites.allBetween(c11, new SimpleComposite("alien"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void reversed() {
    Composites.allBetween(c12, c11);
  }

  @Test
  public void neighbors() {
    assertBetween(c11, c12, Collections.<SimpleComposite>emptyList());
    assertBetween(c3331, c3335, asList(c3332, c3333, c3334));
    assertBetween(c3332, c3335, asList(c3333, c3334));
    assertBetween(c3331, c3334, asList(c3332, c3333));
    assertBetween(c3332, c3334, asList(c3333));
  }

  @Test
  public void down() {
    assertBetween(c311, c3133, asList(c312, c3131, c3132));
    assertBetween(c311, c3132, asList(c312, c3131));
    assertBetween(c312, c3133, asList(c3131, c3132));
    assertBetween(c312, c3132, asList(c3131));
  }

  @Test
  public void up() {
    assertBetween(c11, c21, asList(c12));
    assertBetween(c12, c21, Collections.<SimpleComposite>emptyList());
    assertBetween(c12, c3131, asList(c21, c2, c311, c312));
    assertBetween(c12, c32, asList(c21, c2, c311, c312, c3131, c3132, c3133, c313, c31));
    assertBetween(c3132, c332, asList(c3133, c32, c331));
    assertBetween(c3132, c3335, asList(c3133, c32, c331, c332, c3331, c3332, c3333, c3334));
    assertBetween(c3132, c3333, asList(c3133, c32, c331, c332, c3331, c3332));
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals(expected, Composites.allBetween(left, right));
  }
}