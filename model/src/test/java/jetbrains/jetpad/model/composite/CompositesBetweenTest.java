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

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CompositesBetweenTest extends BaseTestCase {
  private SimpleCompositesTree tree;

  @Before
  public void init() {
    tree = new SimpleCompositesTree();
  }

  @Test
  public void same() {
    SimpleComposite root = new SimpleComposite("root");
    assertBetween(root, root, Collections.<SimpleComposite>emptyList());
    assertBetween(tree.g, tree.g, Collections.<SimpleComposite>emptyList());
  }

  @Test(expected=IllegalArgumentException.class)
  public void nonExisting() {
    Composites.allBetween(tree.e, new SimpleComposite("alien"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void reversed() {
    Composites.allBetween(tree.f, tree.e);
  }

  @Test
  public void neighbors() {
    assertBetween(tree.e, tree.f, Collections.<SimpleComposite>emptyList());
    assertBetween(tree.u, tree.y, asList(tree.v, tree.w, tree.x));
    assertBetween(tree.v, tree.y, asList(tree.w, tree.x));
    assertBetween(tree.u, tree.x, asList(tree.v, tree.w));
    assertBetween(tree.v, tree.x, asList(tree.w));
  }

  @Test
  public void down() {
    assertBetween(tree.k, tree.t, asList(tree.l, tree.r, tree.s));
    assertBetween(tree.k, tree.s, asList(tree.l, tree.r));
    assertBetween(tree.l, tree.t, asList(tree.r, tree.s));
    assertBetween(tree.l, tree.s, asList(tree.r));
  }

  @Test
  public void up() {
    assertBetween(tree.e, tree.g, asList(tree.f));
    assertBetween(tree.f, tree.g, Collections.<SimpleComposite>emptyList());
    assertBetween(tree.f, tree.r, asList(tree.g, tree.c, tree.k, tree.l));
    assertBetween(tree.f, tree.i, asList(tree.g, tree.c, tree.k, tree.l, tree.r, tree.s, tree.t, tree.m, tree.h));
    assertBetween(tree.s, tree.p, asList(tree.t, tree.i, tree.o));
    assertBetween(tree.s, tree.y, asList(tree.t, tree.i, tree.o, tree.p, tree.u, tree.v, tree.w, tree.x));
    assertBetween(tree.s, tree.w, asList(tree.t, tree.i, tree.o, tree.p, tree.u, tree.v));
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals(expected, Composites.allBetween(left, right));
  }
}