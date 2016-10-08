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
import com.sun.prism.es2.ES2Pipeline;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

class JsonUtil {
  private static final char[] SPECIAL_CHARS = {'\"', '\\', '/', '\b', '\f', '\n', '\r', '\t'};
  private static final String[] ESCAPED_SPECIAL_CHARS = {"\\\"", "\\\\", "\\/", "\\b", "\\f", "\\n", "\\r", "\\t"};
  private static final char[] UNESCAPED_SPECIAL_CHARS = {'"', '\\', '/', 'b', 'f', 'n', 'r', 't'};

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
    for (int i = 0; i < s.length(); i++) {
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
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '\\' && i < s.length() - 1) {
        if (result == null) {
          result = new StringBuilder();
          result.append(s.substring(0, i));
        }

        i++;
        ch = s.charAt(i);
        int index = Chars.indexOf(UNESCAPED_SPECIAL_CHARS, ch);
        if (index != -1) {
          result.append(SPECIAL_CHARS[index]);
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