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

import jetbrains.jetpad.base.Persister;

import java.util.HashMap;
import java.util.Objects;

public class Colors {
  public static final double DEFAULT_FACTOR = 0.7d;

  private static HashMap<String, Color> colorsList = createColorsList();

  private static HashMap<String, Color> createColorsList () {
    HashMap <String, Color> colorList = new HashMap<>();
    colorList.put("white", Color.WHITE);
    colorList.put("black", Color.BLACK);
    colorList.put("light-gray", Color.LIGHT_GRAY);
    colorList.put("very-light-gray", Color.VERY_LIGHT_GRAY);
    colorList.put("gray", Color.GRAY);
    colorList.put("red", Color.RED);
    colorList.put("light-green", Color.LIGHT_GREEN);
    colorList.put("green", Color.GREEN);
    colorList.put("dark-green", Color.DARK_GREEN);
    colorList.put("blue", Color.BLUE);
    colorList.put("dark-blue", Color.DARK_BLUE);
    colorList.put("light-blue", Color.LIGHT_BLUE);
    colorList.put("yellow", Color.YELLOW);
    colorList.put("light-yellow", Color.LIGHT_YELLOW);
    colorList.put("very-light-yellow", Color.VERY_LIGHT_YELLOW);
    colorList.put("magenta", Color.MAGENTA);
    colorList.put("light-magenta", Color.LIGHT_MAGENTA);
    colorList.put("dark-magenta", Color.DARK_MAGENTA);
    colorList.put("cyan", Color.CYAN);
    colorList.put("light-cyan", Color.LIGHT_CYAN);
    colorList.put("orange", Color.ORANGE);
    colorList.put("pink", Color.PINK);
    colorList.put("light-pink", Color.LIGHT_PINK);
    return colorList;
  }

  public static boolean isColorName(String colorName) {
    return colorsList.containsKey(colorName.toLowerCase());
  }

  public static Color forName(String colorName) {
    Color res = colorsList.get(colorName.toLowerCase());
    if (res != null) {
      return res;
    } else {
      throw new IllegalArgumentException();
    }
  }

  public static double generateHueColor() {
    return 360 * Math.random();
  }

  public static Color generateColor(double s, double v) {
    return rgbFromHsv(360 * Math.random(), s, v);
  }

  public static Color rgbFromHsv(double h, double s) {
    return rgbFromHsv(h, s, 1.0);
  }

  public static Color rgbFromHsv(double h, double s, double v) {
    double hd = (h / 60);
    double c = v * s;
    double x = c * (1 - Math.abs(hd % 2 - 1));

    double r = 0;
    double g = 0;
    double b = 0;

    if (hd < 1) {
      r = c;
      g = x;
    } else if (hd < 2) {
      r = x;
      g = c;
    } else if (hd < 3) {
      g = c;
      b = x;
    } else if (hd < 4) {
      g = x;
      b = c;
    } else if (hd < 5) {
      r = x;
      b = c;
    } else {
      r = c;
      b = x;
    }

    double m = v - c;
    return new Color((int) (255 * (r + m)), (int) (255 * (g + m)), (int) (255 * (b + m)));
  }

  public static double[] hsvFromRgb(Color color) {
    double scale = 1.f / 255;
    double r = color.getRed() * scale;
    double g = color.getGreen() * scale;
    double b = color.getBlue() * scale;
    double min = Math.min(r, Math.min(g, b));
    double max = Math.max(r, Math.max(g, b));

    double v = max == 0 ? 0 : 1 - min / max;
    double h, div = 1.f / (6 * (max - min));

    if (max == min) {
      h = 0;
    } else if (max == r) {
      h = g >= b ? (g - b) * div : 1 + (g - b) * div;
    } else if (max == g) {
      h = 1.f / 3 + (b - r) * div;
    } else {
      h = 2.f / 3 + (r - g) * div;
    }

    return new double[]{360 * h, v, max};
  }

  public static Color darker(Color c) {
    return darker(c, DEFAULT_FACTOR);
  }

  public static Color lighter(Color c) {
    return lighter(c, DEFAULT_FACTOR);
  }

  public static Color darker(Color c, double factor) {
    if (c != null) {
      return new Color(
          Math.max((int) (c.getRed() * factor), 0),
          Math.max((int) (c.getGreen() * factor), 0),
          Math.max((int) (c.getBlue() * factor), 0),
          c.getAlpha());
    } else {
      return null;
    }
  }

  public static Color lighter(Color c, double factor) {
    if (c != null) {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();
      int alpha = c.getAlpha();

      int i = (int) (1.0 / (1.0 - factor));
      if (r == 0 && g == 0 && b == 0) {
        return new Color(i, i, i, alpha);
      }
      if (r > 0 && r < i) r = i;
      if (g > 0 && g < i) g = i;
      if (b > 0 && b < i) b = i;

      return new Color(
          Math.min((int) (r / factor), 255),
          Math.min((int) (g / factor), 255),
          Math.min((int) (b / factor), 255),
          alpha);
    } else {
      return null;
    }
  }

  public static Color mimicTransparency(Color color, double alpha, Color background) {
    int red = (int) (color.getRed() * alpha + background.getRed() * (1 - alpha));
    int green = (int) (color.getGreen() * alpha + background.getGreen() * (1 - alpha));
    int blue = (int) (color.getBlue() * alpha + background.getBlue() * (1 - alpha));
    return new Color(red, green, blue);
  }

  public static Color withOpacity(Color c, double opacity) {
    if (opacity < 1d) {
      return c.changeAlpha(Math.max(0, Math.min(255, (int) Math.round(255 * opacity))));
    }
    return c;
  }

  public static double contrast(Color color, Color other) {
    return (luminance(color) + .05) / (luminance(other) + .05);
  }

  public static double luminance(Color color) {
    return .2126 * colorLuminance(color.getRed()) + .7152 * colorLuminance(color.getGreen()) + .0722 * colorLuminance(color.getBlue());
  }

  private static double colorLuminance(int componentValue) {
    return componentValue <= 10 ? componentValue / 3294d : Math.pow(componentValue / 269d + .0513, 2.4);
  }

  public static boolean solid(Color c) {
    return c.getAlpha() == 255;
  }

  public static Color[] distributeEvenly(int count, double saturation) {
    Color result[] = new Color[count];

    int sector = 360 / count;
    for (int i = 0; i < count; i++) {
      result[i] = rgbFromHsv(sector * i, saturation);
    }
    return result;
  }

  public static Persister<Color> colorPersister(final Color defaultValue) {
    return new Persister<Color>() {
      @Override
      public Color deserialize(String value) {
        if (value == null) {
          return defaultValue;
        }
        return Color.parseColor(value);
      }

      @Override
      public String serialize(Color value) {
        if (Objects.equals(value, defaultValue)) {
          return null;
        }
        return value.toString();
      }

      @Override
      public String toString() {
        return "colorPersister";
      }
    };
  }
}
