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

import java.util.Arrays;
import java.util.List;

class JsonUtil {
  static final List<Character> SPECIAL_CHARS = Arrays.asList('\"', '\\', '/', '\b', '\f', '\n', '\r', '\t');
  static final List<String> ESCAPED_SPECIAL_CHARS = Arrays.asList("\\\"", "\\\\", "\\/", "\\b", "\\f", "\\n", "\\r", "\\t");
  static final List<Character> UNESCAPED_SPECIAL_CHARS = Arrays.asList('"', '\\', '/', 'b', 'f', 'n', 'r', 't');

  private JsonUtil() {
  }

  static String escape(String s) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (SPECIAL_CHARS.contains(c)) {
        builder.append(ESCAPED_SPECIAL_CHARS.get(SPECIAL_CHARS.indexOf(c)));
      } else {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  static String unescape(String s) {
    StringBuilder result = null;
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '\\' && i < s.length() - 1) {
        if (result == null) {
          result = new StringBuilder();
          result.append(s.substring(0, i));
        }

        i++;
        ch = s.charAt(i);
        if (UNESCAPED_SPECIAL_CHARS.contains(ch)) {
          result.append(SPECIAL_CHARS.get(UNESCAPED_SPECIAL_CHARS.indexOf(ch)));
        } else if (ch == 'u') {
          if (i >= s.length() - 4) {
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