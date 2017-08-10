/*
 * Copyright 2012-2017 JetBrains s.r.o
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
import java.util.List;

public class Base64Coder {
  private static final Base64Table ourTable = new Base64Table('+', '/');

  public static String encode(byte[] bytes) {
    int blocksLen = bytes.length / 3;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < blocksLen; i++) {
      int base = i * 3;
      int b1 = bytes[base] & 0xFF;
      int b2 = bytes[base + 1] & 0xFF;
      int b3 = bytes[base + 2] & 0xFF;

      result.append(ourTable.valueToCh(b1 >> 2));
      result.append(ourTable.valueToCh(((b1 & 0x3) << 4) + (b2 >> 4)));
      result.append(ourTable.valueToCh(((b2 & 0xF) << 2) + (b3 >> 6)));
      result.append(ourTable.valueToCh(((b3 & 0x3F))));
    }

    int lastBlock = blocksLen * 3 - 1;
    if (bytes.length % 3 == 1) {
      int b = bytes[lastBlock + 1] & 0xFF;
      result.append(ourTable.valueToCh(b >> 2));
      result.append(ourTable.valueToCh((b & 0x3) << 4));
      result.append("==");
    } else if (bytes.length % 3 == 2) {
      int b1 = bytes[lastBlock + 1] & 0xFF;
      int b2 = bytes[lastBlock + 2] & 0xFF;
      result.append(ourTable.valueToCh(b1 >> 2));
      result.append(ourTable.valueToCh(((b1 & 0x3) << 4) + (b2 >> 4)));
      result.append(ourTable.valueToCh(((b2 & 0xF) << 2)));
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

      int b1 = (byte) ourTable.chToValue(c1);
      int b2 = (byte) ourTable.chToValue(c2);
      int b3 = c3 != '=' ? (byte) ourTable.chToValue(c3) : -1;
      int b4 = c4 != '=' ? (byte) ourTable.chToValue(c4) : -1;

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
    byte b = -1;
    System.out.println(b & 0xFF);
  }
}