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

import static jetbrains.jetpad.model.composite.SimpleCompositesTree.a;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.d;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.e;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.g;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.h;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.i;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.m;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.r;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.t;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.y;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CompositesCommonAncestorTest extends BaseTestCase {

  @Test
  public void differentTrees() {
    assertNull(Composites.commonAncestor(c, new SimpleComposite("alien")));
  }

  @Test
  public void same() {
    assertCommonAncestor(a, a, a);
    assertCommonAncestor(d, d, d);
    assertCommonAncestor(y, y, y);
  }

  @Test
  public void ancestor() {
    assertCommonAncestor(a, h, a);
    assertCommonAncestor(c, g, c);
    assertCommonAncestor(d, m, d);
    assertCommonAncestor(d, r, d);
  }

  @Test
  public void sameLevel() {
    assertCommonAncestor(c, d, a);
    assertCommonAncestor(i, h, d);
    assertCommonAncestor(r, t, m);
  }

  @Test
  public void differentLevels() {
    assertCommonAncestor(e, c, a);
    assertCommonAncestor(m, i, d);
    assertCommonAncestor(t, i, d);
  }

  private void assertCommonAncestor(SimpleComposite first, SimpleComposite second, SimpleComposite expected) {
    assertSame(expected, Composites.commonAncestor(first, second));
  }
}