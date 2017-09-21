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

public final class SimpleCompositesTree {

  static final SimpleComposite c0, c1, c2, c3, c11, c12, c21, c31, c32, c33,
      c311, c312, c313, c331, c332, c3131, c3132, c3133, c333, c3331, c3332, c3333, c3334, c3335;

  static {
    c0 = new SimpleComposite("c0",
        c1 = new SimpleComposite("c1",
            c11 = new SimpleComposite("c11"),
            c12 = new SimpleComposite("c12")),
        c2 = new SimpleComposite("c2",
            c21 = new SimpleComposite("c21")),
        c3 = new SimpleComposite("c3",
            c31 = new SimpleComposite("c31",
                c311 = new SimpleComposite("c311"),
                c312 = new SimpleComposite("c312"),
                c313 = new SimpleComposite("c313",
                    c3131 = new SimpleComposite("c3131"),
                    c3132 = new SimpleComposite("c3132"),
                    c3133 = new SimpleComposite("c3133"))),
            c32 = new SimpleComposite("c32"),
            c33 = new SimpleComposite("c33",
                c331 = new SimpleComposite("c331"),
                c332 = new SimpleComposite("c332"),
                c333 = new SimpleComposite("c333",
                    c3331 = new SimpleComposite("c3331"),
                    c3332 = new SimpleComposite("c3332"),
                    c3333 = new SimpleComposite("c3333"),
                    c3334 = new SimpleComposite("c3334"),
                    c3335 = new SimpleComposite("c3335")))));
  }

  private SimpleCompositesTree() {
  }
}