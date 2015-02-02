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

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Persister;

public class Colors {
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
    final double scale = 1.f / 255;
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
        if (Objects.equal(value, defaultValue)) {
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