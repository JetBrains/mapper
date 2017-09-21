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
    assertLine(tree.e, tree.f);
    assertLine(tree.u, tree.v, tree.w, tree.x, tree.y);
  }

  @Test
  public void down() {
    assertLine(tree.k, tree.l, tree.r, tree.s, tree.t);
  }

  @Test
  public void up() {
    assertLine(tree.e, tree.f, tree.g);
    assertBetween(tree.f, tree.r, asList(tree.g, tree.c, tree.k, tree.l));
    assertBetween(tree.f, tree.i, asList(tree.g, tree.c, tree.k, tree.l, tree.r, tree.s, tree.t, tree.m, tree.h));
    assertLine(tree.s, tree.t, tree.i, tree.o, tree.p, tree.u, tree.v, tree.w, tree.x, tree.y);
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals(expected, Composites.allBetween(left, right));
  }

  private void assertLine(SimpleComposite... nodes) {
    List<SimpleComposite> line = asList(nodes);
    int i = 0;
    while (i != line.size() - 1) {
      assertBetween(line.get(i), line.get(i), Collections.<SimpleComposite>emptyList());
      int j = i + 1;
      while (j != line.size()) {
        assertBetween(line.get(i), line.get(j), line.subList(i + 1, j));
        j++;
      }
      i++;
    }
  }
}