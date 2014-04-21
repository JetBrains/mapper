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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Base64Coder {
  private static int ourBase = 0;
  private static final int[] ourChToValue = new int[255];
  private static final char[] ourValueToCh = new char[255];

  static {
    Arrays.fill(ourChToValue, 0, ourChToValue.length, -1);

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
    add('/');

    if (ourBase != 64) throw new IllegalStateException();
  }

  private Base64Coder() {
  }

  private static void add(char ch) {
    int index = ourBase++;
    ourChToValue[ch] = index;
    ourValueToCh[index] = ch;
  }

  public static String encode(byte[] bytes) {
    int blocksLen = bytes.length / 3;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < blocksLen; i++) {
      int base = i * 3;
      int b1 = (bytes[base] & 0xFF);
      int b2 = (bytes[base + 1] & 0xFF);
      int b3 = (bytes[base + 2] & 0xFF);

      result.append(ourValueToCh[b1 >> 2]);
      result.append(ourValueToCh[((b1 & 0x3) << 4) + (b2 >> 4)]);
      result.append(ourValueToCh[((b2 & 0xF) << 2) + (b3 >> 6)]);
      result.append(ourValueToCh[((b3 & 0x3F))]);
    }

    int lastBlock = blocksLen * 3 - 1;
    if (bytes.length % 3 == 1) {
      byte b = bytes[lastBlock + 1];
      result.append(ourValueToCh[b >> 2]);
      result.append(ourValueToCh[(b & 0x3) << 4]);
      result.append("==");
    } else if (bytes.length % 3 == 2) {
      byte b1 = bytes[lastBlock + 1];
      byte b2 = bytes[lastBlock + 2];
      result.append(ourValueToCh[b1 >> 2]);
      result.append(ourValueToCh[((b1 & 0x3) << 4) + (b2 >> 4)]);
      result.append(ourValueToCh[((b2 & 0xF) << 2)]);
      result.append("=");
    }

    return result.toString();
  }

  public static byte[] decodeBytes(String s) {
    if (s.length() % 4 != 0) {
      throw new IllegalArgumentException();
    }

    List<Integer> bytes = new ArrayList<>();
    for (int i = 0; i < s.length() / 4; i++) {
      char c1 = s.charAt(i * 4);
      char c2 = s.charAt(i * 4 + 1);
      char c3 = s.charAt(i * 4 + 2);
      char c4 = s.charAt(i * 4 + 3);

      int b1 = (byte) ourChToValue[c1];
      int b2 = (byte) ourChToValue[c2];
      int b3 = c3 != '=' ? (byte) ourChToValue[c3] : -1;
      int b4 = c4 != '=' ? (byte) ourChToValue[c4] : -1;

      bytes.add(((b1 << 2) + (b2 >> 4)));
      if (c3 != '=') {
        bytes.add((((b2 & 0xF) << 4) + (b3 >> 2)));
      }
      if (c4 != '=') {
        bytes.add((((b3 & 0x3) << 6) + b4));
      }
    }
    byte[] result = new byte[bytes.size()];
    for (int i = 0; i < bytes.size(); i++) {
      result[i] = (byte) ((int) bytes.get(i));
    }
    return result;
  }
}