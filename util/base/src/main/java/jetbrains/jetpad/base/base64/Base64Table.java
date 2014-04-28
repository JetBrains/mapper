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
package jetbrains.jetpad.base.base64;

import java.util.Arrays;

class Base64Table {
  private int myBase = 0;
  private final int[] myChToValue = new int[255];
  private final char[] myValueToCh = new char[255];


  Base64Table(char lastChar) {
    this('-', lastChar);
  }

  Base64Table(char beforeLast, char lastChar) {
    Arrays.fill(myChToValue, 0, myChToValue.length, -1);

    for (char ch = 'A'; ch <= 'Z'; ch++) {
      add(ch);
    }
    for (char ch = 'a'; ch <= 'z'; ch++) {
      add(ch);
    }
    for (char ch = '0'; ch <= '9'; ch++) {
      add(ch);
    }
    add(beforeLast);
    add(lastChar);

    if (myBase != 64) throw new IllegalStateException();
  }

  private void add(char ch) {
    int index = myBase++;
    myChToValue[ch] = index;
    myValueToCh[index] = ch;
  }

  int getBase() {
    return myBase;
  }

  int chToValue(char ch) {
    int result = myChToValue[ch];
    if (result == -1) {
      throw new IllegalArgumentException("Invalid base64 char '" + ch + "'");
    }
    return result;
  }

  char valueToCh(int val) {
    if (val >= myBase) {
      throw new IllegalArgumentException();
    }
    return myValueToCh[val];
  }
}