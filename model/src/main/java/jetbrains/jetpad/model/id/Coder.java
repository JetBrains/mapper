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

import java.util.HashMap;
import java.util.Map;

public class Coder {
  private static final Map<Character, Integer> ourChToValue = new HashMap<Character, Integer>();
  private static final Map<Integer, Character> ourValueToCh = new HashMap<Integer, Character>();

  private Coder() {
  }

  private static void add(char ch) {
    int index = Coder.ourChToValue.size();
    ourChToValue.put(ch, index);
    ourValueToCh.put(index, ch);
  }

  public static String encode(long l) {
    StringBuilder result = new StringBuilder();
    int base = Coder.ourChToValue.size();
    do {
      char ch = ourValueToCh.get((int) (l % base));
      result.insert(0, ch);
      l = l / base;
    } while (l != 0);

    return result.toString();
  }

  public static long decode(String s) {
    long l = 0;
    int base = Coder.ourChToValue.size();
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      Integer val = ourChToValue.get(ch);
      if (val == null) throw new IllegalStateException("Unknown character '" + ch + "'");
      l = l * base + val;
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
  }
}

