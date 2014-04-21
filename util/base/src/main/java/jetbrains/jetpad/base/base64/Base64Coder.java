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
    add('-');

    if (ourBase != 64) throw new IllegalStateException();
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

  public static void main(String[] args) {
    String img = "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";
    decodeBytes(img);
  }
}