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
package jetbrains.jetpad.values;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColorTest {

  @Test
  public void parseHex() {
    assertEquals(Color.RED, Color.parseHex(Color.RED.toHexColor()));
  }

  @Test
  public void parseRgba() {
    assertEquals(Color.RED, Color.parseColor("rgba(255,0,0,255)"));
  }

  @Test
  public void parseRgb() {
    assertEquals(Color.RED, Color.parseColor("rgb(255,0,0)"));
  }

  @Test
  public void parseColor() {
    assertEquals(Color.BLUE, Color.parseColor("color(0,0,255,255)"));
  }

  @Test
  public void parseColorRgb() {
    assertEquals(Color.BLUE, Color.parseColor("color(0,0,255)"));
  }

  @Test
  public void parseRgbWithSpaces() {
    assertEquals(Color.RED, Color.parseColor("rgb(255, 0, 0)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void noLastNumber() {
    Color.parseColor("rgb(255, 0, )");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownPrefix() {
    Color.parseColor("rbg(255, 0, )");
  }
}