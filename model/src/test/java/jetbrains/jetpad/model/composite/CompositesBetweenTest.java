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

import java.util.Collections;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

public class CompositesBetweenTest extends BaseTestCase {
  private SimpleCompositesTree tree;

  @Before
  public void init() {
    tree = new SimpleCompositesTree();
  }

  @Test
  public void same() {
    SimpleComposite root = new SimpleComposite("root");
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(root, root));

    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(tree.g, tree.g));
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
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(tree.e, tree.f));
    assertEquals(asList(tree.v, tree.w, tree.x), Composites.allBetween(tree.u, tree.y));
    assertEquals(asList(tree.w, tree.x), Composites.allBetween(tree.v, tree.y));
    assertEquals(asList(tree.v, tree.w), Composites.allBetween(tree.u, tree.x));
    assertEquals(asList(tree.w), Composites.allBetween(tree.v, tree.x));
  }

  @Test
  public void down() {
    assertEquals(asList(tree.l, tree.r, tree.s), Composites.allBetween(tree.k, tree.t));
    assertEquals(asList(tree.l, tree.r), Composites.allBetween(tree.k, tree.s));
    assertEquals(asList(tree.r, tree.s), Composites.allBetween(tree.l, tree.t));
    assertEquals(asList(tree.r), Composites.allBetween(tree.l, tree.s));
  }

  @Test
  public void up() {
    assertEquals(asList(tree.f), Composites.allBetween(tree.e, tree.g));
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(tree.f, tree.g));
    assertEquals(asList(tree.g, tree.c, tree.k, tree.l), Composites.allBetween(tree.f, tree.r));
    assertEquals(asList(tree.g, tree.c, tree.k, tree.l, tree.r, tree.s, tree.t, tree.m, tree.h),
      Composites.allBetween(tree.f, tree.i));
    assertEquals(asList(tree.t, tree.i, tree.o), Composites.allBetween(tree.s, tree.p));
    assertEquals(asList(tree.t, tree.i, tree.o, tree.p, tree.u, tree.v, tree.w, tree.x),
      Composites.allBetween(tree.s, tree.y));
    assertEquals(asList(tree.t, tree.i, tree.o, tree.p, tree.u, tree.v), Composites.allBetween(tree.s, tree.w));
  }
}
