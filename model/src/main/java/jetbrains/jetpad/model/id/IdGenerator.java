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
package jetbrains.jetpad.model.id;

import java.util.Random;

class IdGenerator {
  private static final Random ourRandom = new Random();

  static String nextBase62RandomId(int length) {
    char[] chars = new char[length];
    int nBits = 0;
    int bits = 0;

    for (int i = 0; i < chars.length; ) {
      if (nBits < 6) {
        nBits = 32;
        bits = ourRandom.nextInt();
      }

      int idx = bits & 63;
      bits >>= 6;
      nBits -= 6;
      if (idx < 62) {
        chars[i++] = toChar(idx);
      }
    }
    return new String(chars);
  }

  private static char toChar(int x) {
    return (char) (x < 10 ? '0' + x : x < 36 ? 'a' + x - 10 : 'A' + x - 36);
  }
}