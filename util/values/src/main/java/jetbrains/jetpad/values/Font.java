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

public class Font {
  private FontFamily myFamily;
  private int mySize;
  private boolean myBold;
  private boolean myItalic;

  public Font(FontFamily family, int size) {
    this(family, size, false, false);
  }

  public Font(FontFamily family, int size, boolean bold, boolean italic) {
    myFamily = family;
    mySize = size;
    myBold = bold;
    myItalic = italic;
  }

  public FontFamily getFamily() {
    return myFamily;
  }

  public int getSize() {
    return mySize;
  }

  public boolean isBold() {
    return myBold;
  }

  public boolean isItalic() {
    return myItalic;
  }

  @Override
  public String toString() {
    return myFamily + " " + mySize + (myBold ? " bold" : "") + (myItalic ? " italic" : "");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Font)) return false;
    Font font = (Font) o;
    return myFamily == font.myFamily && mySize == font.mySize && myBold == font.myBold && myItalic == font.myItalic;
  }

  @Override
  public int hashCode() {
    int result = myFamily.hashCode();
    result = 31 * result + mySize;
    result = 31 * result + (myBold ? 1 : 0);
    result = 31 * result + (myItalic ? 1 : 0);
    return result;
  }
}