/*
 * Copyright 2012-2014 JetBrains s.r.o
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

class StringJsonLexer extends JsonLexer {
  private String myInput;
  private int myPosition;
  private int myTokenStart;
  private int myCurrent;
  private boolean hasReadCurrent;

  StringJsonLexer(String input) {
    super(true);
    myInput = input;
    myPosition = 0;
    hasReadCurrent = false;
    next();
  }

  @Override
  protected int getCurrent() {
    if (!hasReadCurrent) {
      hasReadCurrent = true;
      myCurrent = readCurrent();
    }
    return myCurrent;
  }

  protected String tokenText() {
    return myInput.substring(myTokenStart, myPosition);
  }

  protected void setTokenStart() {
    myTokenStart = myPosition;
  }

  protected int readCurrent() {
    if (myPosition < myInput.length()) {
      return myInput.charAt(myPosition);
    } else {
      return -1;
    }
  }

  protected void advance() {
    myPosition++;
    hasReadCurrent = false;
  }

  protected boolean readString(String s) {
    if (getCurrent() == -1) {
      return false;
    }
    if (myInput.startsWith(s, myPosition)) {
      myPosition += s.length();
      hasReadCurrent = false;
      return true;
    }
    return false;
  }


}