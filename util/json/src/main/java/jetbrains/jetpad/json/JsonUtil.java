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
package jetbrains.jetpad.json;

import com.google.common.primitives.Chars;

import java.util.Arrays;
import java.util.List;

class JsonUtil {
  private static final char[] SPECIAL_CHARS = {'\"', '\\', '/', '\b', '\f', '\n', '\r', '\t'};
  private static final String[] ESCAPED_SPECIAL_CHARS = {"\\\"", "\\\\", "\\/", "\\b", "\\f", "\\n", "\\r", "\\t"};
  private static final char[] UNESCAPED_SPECIAL_CHARS = {'"', '\\', '/', 'b', 'f', 'n', 'r', 't'};

  private static final int[] UNESCAPED_SPECIAL_CHAR_LOOKUP_TABLE;

  static {
    int[] table = new int[255];
    for (int i = 0; i < table.length; i++) {
      table[i] = -1;
    }
    for (int i = 0; i < UNESCAPED_SPECIAL_CHARS.length; i++) {
      table[UNESCAPED_SPECIAL_CHARS[i]] = SPECIAL_CHARS[i];
    }
    UNESCAPED_SPECIAL_CHAR_LOOKUP_TABLE = table;
  }

  private JsonUtil() {
  }

  static List<Character> getSpecialChars() {
    return Chars.asList(SPECIAL_CHARS);
  }

  static List<String> getEscapedSpecialChars() {
    return Arrays.asList(ESCAPED_SPECIAL_CHARS);
  }

  static boolean isUnescapedSpecialChar(char ch) {
    return Chars.indexOf(UNESCAPED_SPECIAL_CHARS, ch) != -1;
  }

  static String escape(String s) {
    StringBuilder builder = new StringBuilder();
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);

      int index = Chars.indexOf(SPECIAL_CHARS, c);
      if (index != -1) {
        builder.append(ESCAPED_SPECIAL_CHARS[index]);
      } else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  static String unescape(String s) {
    StringBuilder result = null;
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      if (ch == '\\' && i < len - 1) {
        if (result == null) {
          result = new StringBuilder();
          result.append(s.substring(0, i));
        }

        i++;
        ch = s.charAt(i);
        int specialChar = -1;
        if (ch >= 0 && ch < UNESCAPED_SPECIAL_CHAR_LOOKUP_TABLE.length) {
          specialChar = UNESCAPED_SPECIAL_CHAR_LOOKUP_TABLE[ch];
        }
        if (specialChar != -1) {
          result.append((char) specialChar);
        } else if (ch == 'u') {
          if (i >= len - 4) {
            throw new RuntimeException();
          }
          String hexNumber = s.substring(i + 1, i + 5);
          result.append((char) Integer.parseInt(hexNumber, 16));
          i += 4;
        } else {
          throw new RuntimeException();
        }
      } else {
        if (result != null) {
          result.append(ch);
        }
      }
    }

    if (result != null) {
      return result.toString();
    } else {
      return s;
    }
  }
}
