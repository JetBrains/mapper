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
package jetbrains.jetpad.json;

class IndentBuilder {
  private StringBuilder myBuilder = new StringBuilder();
  private int myIndentSize;
  private int myCurrentIndent;

  IndentBuilder(int indentSize) {
    myIndentSize = indentSize;
  }

  IndentBuilder append(Object o) {
    myBuilder.append(o);
    return this;
  }

  void newLine() {
    if (myIndentSize > 0) {
      myBuilder.append("\n");
      nSpaces(myCurrentIndent * myIndentSize);
    }
  }

  void indent() {
    myCurrentIndent++;
  }

  void unindent() {
    myCurrentIndent--;
  }

  public String toString() {
    return myBuilder.toString();
  }

  private void nSpaces(int n) {
    for (int i = 0; i < n; i++) {
      myBuilder.append(' ');
    }
  }
}