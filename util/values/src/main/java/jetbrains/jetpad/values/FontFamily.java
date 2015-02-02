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

public class FontFamily {
  public static final FontFamily MONOSPACED = new MonospacedFontFamily();

  public static FontFamily forName(String name) {
    return new NamedFontFamily(name);
  }

  private FontFamily() {
  }

  static class NamedFontFamily extends FontFamily {
    private String myName;

    NamedFontFamily(String name) {
      myName = name;
    }

    @Override
    public String toString() {
      return myName;
    }
  }

  private static class MonospacedFontFamily extends FontFamily {
    @Override
    public String toString() {
      return "Monospaced";
    }
  }
}