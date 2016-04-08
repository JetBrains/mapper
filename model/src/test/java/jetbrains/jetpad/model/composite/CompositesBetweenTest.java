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
import java.util.List;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

public class CompositesBetweenTest extends BaseTestCase {
  private SimpleComposite c, e, f, g, h, i, k, l, m, o, p, r, s, t, u, v, w, x, y;

  @Before
  public void init() {
    new SimpleComposite("a",
      new SimpleComposite("b",
        e = new SimpleComposite("e"),
        f = new SimpleComposite("f")),
      c = new SimpleComposite("c",
        g = new SimpleComposite("g")),
      new SimpleComposite("d",
        h = new SimpleComposite("h",
          k = new SimpleComposite("k"),
          l = new SimpleComposite("l"),
          m = new SimpleComposite("m",
            r = new SimpleComposite("r"),
            s = new SimpleComposite("s"),
            t = new SimpleComposite("t"))),
        i = new SimpleComposite("i"),
        new SimpleComposite("j",
          o = new SimpleComposite("o"),
          p = new SimpleComposite("p"),
          new SimpleComposite("q",
            u = new SimpleComposite("u"),
            v = new SimpleComposite("v"),
            w = new SimpleComposite("w"),
            x = new SimpleComposite("x"),
            y = new SimpleComposite("y")))));
  }

  @Test
  public void same() {
    SimpleComposite root = new SimpleComposite("root");
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(root, root));

    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(g, g));
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
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(e, f));
    assertEquals(asList(v, w, x), Composites.allBetween(u, y));
    assertEquals(asList(w, x), Composites.allBetween(v, y));
    assertEquals(asList(v, w), Composites.allBetween(u, x));
    assertEquals(asList(w), Composites.allBetween(v, x));
  }

  @Test
  public void down() {
    assertEquals(asList(l, r, s), Composites.allBetween(k, t));
    assertEquals(asList(l, r), Composites.allBetween(k, s));
    assertEquals(asList(r, s), Composites.allBetween(l, t));
    assertEquals(asList(r), Composites.allBetween(l, s));
  }

  @Test
  public void up() {
    assertEquals(asList(f), Composites.allBetween(e, g));
    assertEquals(Collections.<SimpleComposite>emptyList(), Composites.allBetween(f, g));
    assertEquals(asList(g, c, k, l), Composites.allBetween(f, r));
    assertEquals(asList(g, c, k, l, r, s, t, m, h), Composites.allBetween(f, i));
    assertEquals(asList(t, i, o), Composites.allBetween(s, p));
    assertEquals(asList(t, i, o, p, u, v, w, x), Composites.allBetween(s, y));
    assertEquals(asList(t, i, o, p, u, v), Composites.allBetween(s, w));
  }

  private static class SimpleComposite implements NavComposite<SimpleComposite> {
    private final String name;
    private SimpleComposite parent;
    private List<SimpleComposite> children;

    public SimpleComposite(String name, SimpleComposite... children) {
      this.name = name;
      this.children = asList(children);
      for (SimpleComposite c : children) {
        c.parent = this;
      }
    }

    @Override
    public SimpleComposite nextSibling() {
      if (parent == null) {
        return null;
      } else {
        int index = parent.children().indexOf(this);
        if (index < 0) {
          throw new IllegalStateException("SimpleComposite isn't a child of it's parent.");
        } else if (index == parent.children.size() - 1) {
          return null;
        } else {
          return parent.children().get(index + 1);
        }
      }
    }

    @Override
    public SimpleComposite prevSibling() {
      if (parent == null) {
        return null;
      } else {
        int index = parent.children().indexOf(this);
        if (index < 0) {
          throw new IllegalStateException("SimpleComposite isn't a child of it's parent.");
        } else if (index == 0) {
          return null;
        } else {
          return parent.children().get(index - 1);
        }
      }
    }

    @Override
    public SimpleComposite firstChild() {
      return children.isEmpty() ? null : children.get(0);
    }

    @Override
    public SimpleComposite lastChild() {
      return children.isEmpty() ? null : children.get(children.size() - 1);
    }

    @Override
    public List<SimpleComposite> children() {
      return children;
    }

    @Override
    public SimpleComposite getParent() {
      return parent;
    }

    @Override
    public String toString() {
      return "Composite(" +name + ')';
    }
  }
}
