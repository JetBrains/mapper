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
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.c;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.e;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.f;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.g;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.h;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.i;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.k;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.l;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.m;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.o;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.p;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.r;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.s;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.t;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.u;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.v;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.w;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.x;
import static jetbrains.jetpad.model.composite.SimpleCompositesTree.y;
import static org.junit.Assert.assertEquals;

public class CompositesBetweenTest extends BaseTestCase {

  @Before
  public void init() {
  }

  @Test
  public void same() {
    SimpleComposite root = new SimpleComposite("root");
    assertBetween(root, root, Collections.<SimpleComposite>emptyList());
    assertBetween(g, g, Collections.<SimpleComposite>emptyList());
  }

  @Test(expected=IllegalArgumentException.class)
  public void nonExisting() {
    Composites.allBetween(e, new SimpleComposite("alien"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void reversed() {
    Composites.allBetween(f, e);
  }

  @Test
  public void neighbors() {
    assertBetween(e, f, Collections.<SimpleComposite>emptyList());
    assertBetween(u, y, asList(v, w, x));
    assertBetween(u, x, asList(v, w));
    assertBetween(v, y, asList(w, x));
    assertBetween(v, x, asList(w));
  }

  @Test
  public void down() {
    assertBetween(k, t, asList(l, r, s));
    assertBetween(k, s, asList(l, r));
    assertBetween(l, t, asList(r, s));
    assertBetween(l, s, asList(r));
  }

  @Test
  public void up() {
    assertBetween(e, g, asList(f));
    assertBetween(f, g, Collections.<SimpleComposite>emptyList());
    assertBetween(f, r, asList(g, c, k, l));
    assertBetween(f, i, asList(g, c, k, l, r, s, t, m, h));
    assertBetween(s, p, asList(t, i, o));
    assertBetween(s, y, asList(t, i, o, p, u, v, w, x));
    assertBetween(s, w, asList(t, i, o, p, u, v));
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals(expected, Composites.allBetween(left, right));
  }
}