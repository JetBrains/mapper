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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompositesCommonAncestorTest extends BaseTestCase {
  private SimpleCompositesTree tree;

  @Before
  public void init() {
    tree = new SimpleCompositesTree();
  }

  @Test
  public void diffrentTrees() {
    assertNull(Composites.commonAncestor(tree.c, new SimpleComposite("alien")));
  }

  @Test
  public void same() {
    assertCommonAncestor(tree.a, tree.a, tree.a);
    assertCommonAncestor(tree.d, tree.d, tree.d);
    assertCommonAncestor(tree.y, tree.y, tree.y);
  }

  @Test
  public void ancestor() {
    assertCommonAncestor(tree.a, tree.h, tree.a);
    assertCommonAncestor(tree.c, tree.g, tree.c);
    assertCommonAncestor(tree.d, tree.m, tree.d);
    assertCommonAncestor(tree.d, tree.r, tree.d);
  }

  private void assertCommonAncestor(SimpleComposite first, SimpleComposite second, SimpleComposite expected) {
    assertSame(expected, Composites.commonAncestor(first, second));
  }

  @Test
  public void sameLevel() {
    assertCommonAncestor(tree.c, tree.d, tree.a);
    assertCommonAncestor(tree.i, tree.h, tree.d);
    assertCommonAncestor(tree.r, tree.t, tree.m);
  }

  @Test
  public void differentLevels() {
    assertCommonAncestor(tree.e, tree.c, tree.a);
    assertCommonAncestor(tree.m, tree.i, tree.d);
    assertCommonAncestor(tree.t, tree.i, tree.d);
  }
}