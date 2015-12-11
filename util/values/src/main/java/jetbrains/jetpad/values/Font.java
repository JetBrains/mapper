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

public class Font {
  public final FontFamily family;
  public final int size;
  public final boolean bold;
  public final boolean italic;

  public Font(FontFamily family, int size) {
    this(family, size, false, false);
  }

  public Font(FontFamily family, int size, boolean bold, boolean italic) {
    if (family == null) {
      throw new IllegalArgumentException("Null font family");
    }
    this.family = family;
    this.size = size;
    this.bold = bold;
    this.italic = italic;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Font)) return false;

    Font font = (Font) o;
    if (size != font.size) return false;
    if (bold != font.bold) return false;
    if (italic != font.italic) return false;
    return family.toString().equals(font.family.toString());
  }

  @Override
  public int hashCode() {
    int result = family.toString().hashCode();
    result = 31 * result + size;
    result = 31 * result + (bold ? 1 : 0);
    result = 31 * result + (italic ? 1 : 0);
    return result;
  }
}