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

public class SimpleCompositesTree {
  public SimpleComposite a, c, d, e, f, g, h, i, k, l, m, o, p, r, s, t, u, v, w, x, y;

  public SimpleCompositesTree() {
    a = new SimpleComposite("a",
      new SimpleComposite("b",
        e = new SimpleComposite("e"),
        f = new SimpleComposite("f")),
      c = new SimpleComposite("c",
        g = new SimpleComposite("g")),
      d = new SimpleComposite("d",
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
}