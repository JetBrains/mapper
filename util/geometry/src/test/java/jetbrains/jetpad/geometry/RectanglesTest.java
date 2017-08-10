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
package jetbrains.jetpad.geometry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RectanglesTest {
  private static final Rectangle INNER = new Rectangle(10, 20, 30, 40);
  private static final Rectangle OUTER = new Rectangle(0, 5, 100, 200);

  @Test
  public void upperDistance() {
    assertEquals(15, Rectangles.upperDistance(INNER, OUTER));
  }

  @Test
  public void lowerDistance() {
    assertEquals(145, Rectangles.lowerDistance(INNER, OUTER));
  }

  @Test
  public void leftDistance() {
    assertEquals(10, Rectangles.leftDistance(INNER, OUTER));
  }

  @Test
  public void rightDistance() {
    assertEquals(60, Rectangles.rightDistance(INNER, OUTER));
  }

  @Test(expected = IllegalArgumentException.class)
  public void badUpperDistance() {
    Rectangles.upperDistance(OUTER, INNER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badLowerDistance() {
    Rectangles.lowerDistance(OUTER, INNER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badLeftDistance() {
    Rectangles.leftDistance(OUTER, INNER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void badRightDistance() {
    Rectangles.rightDistance(OUTER, INNER);
  }

  @Test
  public void extendUp() {
    assertEquals(new Rectangle(new Vector(10, 19), new Vector(30, 41)), Rectangles.extendUp(INNER, 1));
  }

  @Test
  public void extendDown() {
    assertEquals(new Rectangle(new Vector(10, 20), new Vector(30, 41)), Rectangles.extendDown(INNER, 1));
  }

  @Test
  public void extendLeft() {
    assertEquals(new Rectangle(new Vector(9, 20), new Vector(31, 40)), Rectangles.extendLeft(INNER, 1));
  }

  @Test
  public void extendRight() {
    assertEquals(new Rectangle(new Vector(10, 20), new Vector(31, 40)), Rectangles.extendRight(INNER, 1));
  }

  @Test
  public void extendSides() {
    assertEquals(new Rectangle(new Vector(9, 20), new Vector(32, 40)), Rectangles.extendSides(1, INNER, 1));
  }

  @Test
  public void shrinkRight() {
    assertEquals(new Rectangle(new Vector(10, 20), new Vector(29, 40)), Rectangles.shrinkRight(INNER, 1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shrinkRightIncorrect() {
    Rectangles.shrinkRight(INNER, INNER.dimension.x + 1);
  }
}