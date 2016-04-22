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
    assertSame(tree.a, Composites.commonAncestor(tree.a, tree.a));
    assertSame(tree.d, Composites.commonAncestor(tree.d, tree.d));
    assertSame(tree.y, Composites.commonAncestor(tree.y, tree.y));
  }

  @Test
  public void ancestor() {
    assertSame(tree.a, Composites.commonAncestor(tree.a, tree.h));
    assertSame(tree.c, Composites.commonAncestor(tree.c, tree.g));
    assertSame(tree.d, Composites.commonAncestor(tree.d, tree.m));
    assertSame(tree.d, Composites.commonAncestor(tree.d, tree.r));
  }

  @Test
  public void sameLevel() {
    assertSame(tree.a, Composites.commonAncestor(tree.c, tree.d));
    assertSame(tree.d, Composites.commonAncestor(tree.i, tree.h));
    assertSame(tree.m, Composites.commonAncestor(tree.r, tree.t));
  }

  @Test
  public void differentLevels() {
    assertSame(tree.a, Composites.commonAncestor(tree.e, tree.c));
    assertSame(tree.d, Composites.commonAncestor(tree.m, tree.i));
    assertSame(tree.d, Composites.commonAncestor(tree.t, tree.i));
  }
}
