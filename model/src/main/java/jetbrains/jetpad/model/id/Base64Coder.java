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
package jetbrains.jetpad.model.id;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Base64Coder {
  private static int ourBase = 0;
  private static final int[] ourChToValue = new int[255];
  private static final char[] ourValueToCh = new char[255];

  static {
    Arrays.fill(ourChToValue, 0, ourChToValue.length, -1);
  }

  private Base64Coder() {
  }

  private static void add(char ch) {
    int index = ourBase++;
    ourChToValue[ch] = index;
    ourValueToCh[index] = ch;
  }

  public static String encode(long l) {
    StringBuilder result = new StringBuilder();
    int base = ourBase;
    do {
      char ch = ourValueToCh[(int) (l % base)];
      result.insert(0, ch);
      l = l >> 6;
    } while (l != 0);

    return result.toString();
  }

  public static long decode(String s) {
    long l = 0;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      int val = ourChToValue[ch];
      if (val == -1) throw new IllegalStateException("Unknown character '" + ch + "'");
      l = (l << 6) + val;
      if (l < 0) throw new RuntimeException("Overflow");
    }
    return l;
  }

  static {
    for (char ch = 'A'; ch <= 'Z'; ch++) {
      add(ch);
    }
    for (char ch = 'a'; ch <= 'z'; ch++) {
      add(ch);
    }
    for (char ch = '0'; ch <= '9'; ch++) {
      add(ch);
    }
    add('+');
    add('-');

    if (ourBase != 64) throw new IllegalStateException();
  }

  public static void main(String[] args) {
    System.out.println(3 << 6);
  }
}

