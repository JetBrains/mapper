/*
 * Copyright 2012-2015 JetBrains s.r.o
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

public class Color {
  public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
  public static final Color WHITE = new Color(255, 255, 255);
  public static final Color BLACK = new Color(0, 0, 0);
  public static final Color LIGHT_GRAY = new Color(192, 192, 192);
  public static final Color VERY_LIGHT_GRAY = new Color(210, 210, 210);
  public static final Color GRAY = new Color(128, 128, 128);
  public static final Color RED = new Color(255, 0, 0);
  public static final Color LIGHT_GREEN= new Color(210, 255, 210);
  public static final Color GREEN = new Color(0, 255, 0);
  public static final Color DARK_GREEN = new Color(0, 128, 0);
  public static final Color BLUE = new Color(0, 0, 255);
  public static final Color DARK_BLUE = new Color(0, 0, 128);
  public static final Color LIGHT_BLUE = new Color(210, 210, 255);
  public static final Color YELLOW = new Color(255, 255, 0);
  public static final Color LIGHT_YELLOW = new Color(255, 255, 128);
  public static final Color VERY_LIGHT_YELLOW = new Color(255, 255, 210);
  public static final Color MAGENTA = new Color(255, 0, 255);
  public static final Color LIGHT_MAGENTA = new Color(255, 210, 255);
  public static final Color DARK_MAGENTA = new Color(128, 0, 128);
  public static final Color CYAN = new Color(0, 255, 255);
  public static final Color LIGHT_CYAN = new Color(210, 255, 255);
  public static final Color ORANGE = new Color(255, 192, 0);
  public static final Color PINK = new Color(255, 175, 175);
  public static final Color LIGHT_PINK = new Color(255, 210, 210);

  private int myRed;
  private int myGreen;
  private int myBlue;
  private int myAlpha;

  public Color(int r, int g, int b, int a) {
    myRed = r;
    myGreen = g;
    myBlue = b;
    myAlpha = a;
  }

  public Color(int r, int g, int b) {
    this(r, g, b, 255);
  }

  public int getRed() {
    return myRed;
  }

  public int getGreen() {
    return myGreen;
  }

  public int getBlue() {
    return myBlue;
  }

  public int getAlpha() {
    return myAlpha;
  }

  public Color changeAlpha(int newAlpha) {
    return new Color(myRed, myGreen, myBlue, newAlpha);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof Color)) {
      return false;
    }

    Color that = (Color) o;
    if (myRed != that.myRed) {
      return false;
    }
    if (myGreen != that.myGreen) {
      return false;
    }
    if (myBlue != that.myBlue) {
      return false;
    }
    if (myAlpha != that.myAlpha) {
      return false;
    }

    return true;
  }

  public String toCssColor() {
    if (getAlpha() == 255) {
      return "rgb(" + getRed() + "," + getGreen() + "," + getBlue() + ")";
    } else {
      return "rgba(" + getRed() + "," + getGreen() + "," + getBlue() + "," + (getAlpha() / 255.0) + ")";
    }
  }

  public String toHexColor() {
    return "#" + toColorPart(myRed) + toColorPart(myGreen) + toColorPart(myBlue);
  }

  private static String toColorPart(int value) {
    if (value < 0 || value > 255) {
      throw new IllegalArgumentException();
    }

    String result = Integer.toHexString(value);
    if (result.length() == 1) {
      return "0" + result;
    } else {
      return result;
    }
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = 31 * result + myRed;
    result = 31 * result + myGreen;
    result = 31 * result + myBlue;
    result = 31 * result + myAlpha;
    return result;
  }

  @Override
  public String toString() {
    return "color(" + myRed + "," + myGreen + "," + myBlue + "," + myAlpha + ")";
  }

  public static Color parseColor(String text) {
    int firstParen = text.indexOf("(");
    if (firstParen == -1) {
      throw new IllegalArgumentException();
    }
    int firstComma = text.indexOf(",", firstParen + 1);
    if (firstComma == -1) {
      throw new IllegalArgumentException();
    }
    int secondComma = text.indexOf(",", firstComma + 1);
    if (secondComma == -1) {
      throw new IllegalArgumentException();
    }
    int thirdComma = text.indexOf(",", secondComma + 1);
    if (thirdComma == -1) {
      throw new IllegalArgumentException();
    }
    int lastParen = text.indexOf(")", thirdComma + 1);
    if (lastParen == -1) {
      throw new IllegalArgumentException();
    }

    int red = Integer.parseInt(text.substring(firstParen + 1, firstComma));
    int green = Integer.parseInt(text.substring(firstComma + 1, secondComma));
    int blue = Integer.parseInt(text.substring(secondComma + 1, thirdComma));
    int alpha = Integer.parseInt(text.substring(thirdComma + 1, lastParen));

    return new Color(red, green, blue, alpha);
  }
}