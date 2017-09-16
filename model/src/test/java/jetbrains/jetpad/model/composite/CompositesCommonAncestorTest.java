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

import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c0;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c11;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c2;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c21;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c31;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c313;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3131;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3133;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c32;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c3335;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CompositesCommonAncestorTest extends BaseTestCase {

  @Test
  public void differentTrees() {
    assertNull(Composites.commonAncestor(c2, new SimpleComposite("alien")));
  }

  @Test
  public void same() {
    assertCommonAncestor(c0, c0, c0);
    assertCommonAncestor(c3, c3, c3);
    assertCommonAncestor(c3335, c3335, c3335);
  }

  @Test
  public void ancestor() {
    assertCommonAncestor(c0, c31, c0);
    assertCommonAncestor(c2, c21, c2);
    assertCommonAncestor(c3, c313, c3);
    assertCommonAncestor(c3, c3131, c3);
  }

  @Test
  public void sameLevel() {
    assertCommonAncestor(c2, c3, c0);
    assertCommonAncestor(c32, c31, c3);
    assertCommonAncestor(c3131, c3133, c313);
  }

  @Test
  public void differentLevels() {
    assertCommonAncestor(c11, c2, c0);
    assertCommonAncestor(c313, c32, c3);
    assertCommonAncestor(c3133, c32, c3);
  }

  private void assertCommonAncestor(SimpleComposite first, SimpleComposite second, SimpleComposite expected) {
    assertSame(expected, Composites.commonAncestor(first, second));
  }
}