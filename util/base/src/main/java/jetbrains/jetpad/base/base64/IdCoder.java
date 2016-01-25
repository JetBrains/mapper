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
package jetbrains.jetpad.base.base64;

class IdCoder {
  private Base64Table myTable;

  IdCoder(Base64Table table) {
    myTable = table;
  }

  String encode(long l) {
    StringBuilder result = new StringBuilder();
    int base = myTable.getBase();
    do {
      char ch = myTable.valueToCh((int) (l % base));
      result.insert(0, ch);
      l = l >> 6;
    } while (l != 0);

    return result.toString();
  }

  long decode(String s) {
    long l = 0;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      int val = myTable.chToValue(ch);
      l = (l << 6) + val;
      if (l < 0) {
        throw new RuntimeException("Overflow");
      }
    }
    return l;
  }
}