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

  private SimpleComposite c;
  private SimpleComposite e;
  private SimpleComposite f;
  private SimpleComposite g;
  private SimpleComposite h;
  private SimpleComposite i;
  private SimpleComposite k;
  private SimpleComposite l;
  private SimpleComposite m;
  private SimpleComposite o;
  private SimpleComposite p;
  private SimpleComposite r;
  private SimpleComposite s;
  private SimpleComposite t;
  private SimpleComposite u;
  private SimpleComposite v;
  private SimpleComposite w;
  private SimpleComposite x;
  private SimpleComposite y;

  @Before
  public void init() {
    SimpleCompositesTree tree = new SimpleCompositesTree();
    c = tree.getC();
    e = tree.getE();
    f = tree.getF();
    g = tree.getG();
    h = tree.getH();
    i = tree.getI();
    k = tree.getK();
    l = tree.getL();
    m = tree.getM();
    o = tree.getO();
    p = tree.getP();
    r = tree.getR();
    s = tree.getS();
    t = tree.getT();
    u = tree.getU();
    v = tree.getV();
    w = tree.getW();
    x = tree.getX();
    y = tree.getY();
  }

  @Test
  public void sameRoot() {
    SimpleComposite root = new SimpleComposite("root");
    assertBetween(root, root, list());
  }

  @Test
  public void sameLeaf() {
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
  public void twoSiblings() {
    assertConsecutiveNodes(e, f);
  }

  @Test
  public void fiveSiblings() {
    assertConsecutiveNodes(u, v, w, x, y);
  }

  @Test
  public void down() {
    assertConsecutiveNodes(k, l, r, s, t);
  }

  @Test
  public void cousins() {
    assertConsecutiveNodes(e, f, g);
  }

  @Test
  public void upSidewaysAndDown() {
    assertBetween(f, c, list());
    assertBetween(f, g, list());
    assertBetween(f, k, list(g, c));
    assertBetween(f, r, list(g, c, k, l));
    assertBetween(f, i, list(g, c, k, l, r, s, t, m, h));
  }

  @Test
  public void upAndDown() {
    assertConsecutiveNodes(s, t, i, o, p, u, v, w, x, y);
  }

  private void assertConsecutiveNodes(SimpleComposite... nodes) {
    List<SimpleComposite> nodeList = list(nodes);
    for (int i = 0; i < nodeList.size(); i++) {
      SimpleComposite left = nodeList.get(i);
      for (int j = i + 1; j < nodeList.size(); j++) {
        SimpleComposite right = nodeList.get(j);
        List<SimpleComposite> expectedSubList = nodeList.subList(i + 1, j);
        assertBetween(left, right, expectedSubList);
      }
    }
  }

  private void assertBetween(SimpleComposite left, SimpleComposite right, List<SimpleComposite> expected) {
    assertEquals("left: " + left + ", right: " + right, expected, Composites.allBetween(left, right));
  }

  private List<SimpleComposite> list(SimpleComposite... simpleComposites) {
    return Arrays.asList(simpleComposites);
  }

}
