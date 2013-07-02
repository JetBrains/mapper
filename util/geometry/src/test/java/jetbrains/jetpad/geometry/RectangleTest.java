/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import static org.junit.Assert.*;

public class RectangleTest {
  @Test
  public void equals() {
    assertEquals(new Rectangle(new Vector(1, 2), new Vector(2, 2)), new Rectangle(new Vector(1, 2), new Vector(2, 2)));
    assertFalse(new Rectangle(new Vector(1, 2), new Vector(2, 2)).equals(new Rectangle(new Vector(1, 2), new Vector(2, 3))));
  }

  @Test
  public void add() {
    assertEquals(new Rectangle(new Vector(1, 2), new Vector(2, 2)), new Rectangle(new Vector(0, 0), new Vector(2, 2)).add(new Vector(1, 2)));
  }

  @Test
  public void sub() {
    assertEquals(new Rectangle(new Vector(0, 0), new Vector(2, 2)), new Rectangle(new Vector(1, 2), new Vector(2, 2)).sub(new Vector(1, 2)));
  }

  @Test
  public void contains() {
    Rectangle rect = new Rectangle(new Vector(0, 0), new Vector(1, 2));

    assertFalse(rect.contains(new Vector(-1, -1)));
    assertTrue(rect.contains(new Vector(1, 1)));
  }

  @Test
  public void intersects() {
    assertFalse(new Rectangle(0, 0, 1, 1).intersects(new Rectangle(2, 2, 1, 1)));
    assertTrue(new Rectangle(0, 0, 2, 2).intersects(new Rectangle(1, 1, 2, 2)));
  }

  @Test
  public void union() {
    assertEquals(new Rectangle(0, 0, 3, 3), new Rectangle(0, 0, 1, 1).union(new Rectangle(2, 2, 1, 1)));
  }
}
