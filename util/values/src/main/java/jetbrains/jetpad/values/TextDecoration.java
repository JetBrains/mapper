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
package jetbrains.jetpad.values;

public class TextDecoration {
  public final Kind kind;
  public final Color color;
  public final int startPos;
  public final int endPos;

  public TextDecoration(Kind kind, Color color, int startPos, int endPos) {
    if (kind == null) {
      throw new IllegalArgumentException("Null kind is not allowed");
    }
    if (color == null) {
      throw new IllegalArgumentException("Null color is not allowed");
    }
    if (startPos < 0) {
      throw new IllegalArgumentException("Negative positions are not allowed: startPos=" + startPos);
    }
    if (endPos < 0) {
      throw new IllegalArgumentException("Negative positions are not allowed: endPos=" + endPos);
    }
    if (startPos > endPos) {
      throw new IllegalArgumentException("startPos is greater than endPos: " +
          "startPos=" + startPos + ", endPos=" + endPos);
    }
    this.kind = kind;
    this.color = color;
    this.startPos = startPos;
    this.endPos = endPos;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = hash * 37 + kind.hashCode();
    hash = hash * 37 + color.hashCode();
    hash = hash * 37 + startPos;
    hash = hash * 37 + endPos;
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof TextDecoration) {
      TextDecoration that = (TextDecoration) o;
      return kind.equals(that.kind)
          && color.equals(that.color)
          && startPos == that.startPos
          && endPos == that.endPos;
    } else {
      return false;
    }
  }

  public enum Kind {
    BACKGROUND_HIGHLIGHT
  }
}
