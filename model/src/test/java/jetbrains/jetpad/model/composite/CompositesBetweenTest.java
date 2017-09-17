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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CompositesBetweenTest extends BaseTestCase {
  private SimpleComposite e;
  private SimpleComposite f;
  private SimpleComposite u;
  private SimpleComposite y;
  private SimpleComposite v;
  private SimpleComposite w;
  private SimpleComposite x;
  private SimpleComposite k;
  private SimpleComposite t;
  private SimpleComposite l;
  private SimpleComposite r;
  private SimpleComposite s;
  private SimpleComposite g;
  private SimpleComposite c;
  private SimpleComposite i;
  private SimpleComposite m;
  private SimpleComposite h;
  private SimpleComposite p;
  private SimpleComposite o;

  @Before
  public void init() {
    SimpleCompositesTree tree = new SimpleCompositesTree();
    c = tree.c;
    e = tree.e;
    f = tree.f;
    g = tree.g;
    h = tree.h;
    i = tree.i;
    k = tree.k;
    l = tree.l;
    m = tree.m;
    o = tree.o;
    r = tree.r;
    p = tree.p;
    s = tree.s;
    t = tree.t;
    u = tree.u;
    v = tree.v;
    w = tree.w;
    x = tree.x;
    y = tree.y;
  }

  @Test
  public void same1() {
    SimpleComposite root = new SimpleComposite("root");
    assertBetween(root, root, list());
  }

  @Test
  public void same2() {
    assertBetween(g, g, list());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nonExisting() {
    Composites.allBetween(e, new SimpleComposite("alien"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void reversed() {
    Composites.allBetween(f, e);
  }

  @Test
  public void neighbors1() {
    assertBetween(e, f, list());
  }

  @Test
  public void neighbors2() {
    assertBetween(u, y, list(v, w, x));
  }

  @Test
  public void neighbors3() {
    assertBetween(v, y, list(w, x));
  }

  @Test
  public void neighbors4() {
    assertBetween(u, x, list(v, w));
  }

  @Test
  public void neighbors5() {
    assertBetween(v, x, list(w));
  }

  @Test
  public void down1() {
    assertBetween(k, t, list(l, r, s));
  }

  @Test
  public void down2() {
    assertBetween(k, s, list(l, r));
  }

  @Test
  public void down3() {
    assertBetween(l, t, list(r, s));
  }

  @Test
  public void down4() {
    assertBetween(l, s, list(r));
  }

  @Test
  public void up1() {
    assertBetween(e, g, list(f));
  }

  @Test
  public void up2() {
    assertBetween(f, g, list());
  }

  @Test
  public void up3() {
    assertBetween(f, r, list(g, c, k, l));
  }

  @Test
  public void up4() {
    assertBetween(f, i, list(g, c, k, l, r, s, t, m, h));
  }

  @Test
  public void up5() {
    assertBetween(s, p, list(t, i, o));
  }

  @Test
  public void up6() {
    assertBetween(s, y, list(t, i, o, p, u, v, w, x));
  }

  @Test
  public void up7() {
    assertBetween(s, w, list(t, i, o, p, u, v));
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals(expected, Composites.allBetween(left, right));
  }

  private List<SimpleComposite> list(SimpleComposite... w) {
    return Arrays.asList(w);
  }
}
