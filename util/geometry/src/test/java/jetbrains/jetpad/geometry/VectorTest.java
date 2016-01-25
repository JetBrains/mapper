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
package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class VectorTest {
  @Test
  public void equals() {
    assertEquals(new Vector(1, 2), new Vector(1, 2));
    assertFalse(new Vector(1, 2).equals(new Vector(2, 1)));
  }

  @Test
  public void add() {
    assertEquals(new Vector(2, 4), new Vector(1, 2).add(new Vector(1, 2)));
  }

  @Test
  public void sub() {
    assertEquals(new Vector(-1, 0), new Vector(1, 2).sub(new Vector(2, 2)));
  }

  @Test
  public void negate() {
    assertEquals(new Vector(-1, -2), new Vector(1, 2).negate());
  }

  @Test
  public void max() {
    assertEquals(new Vector(2, 2), new Vector(1, 2).max(new Vector(2, 1)));
  }

  @Test
  public void min() {
    assertEquals(new Vector(1, 1), new Vector(1, 2).min(new Vector(2, 1)));
  }
}