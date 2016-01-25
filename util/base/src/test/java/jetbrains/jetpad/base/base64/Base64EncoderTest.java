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

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Base64EncoderTest {
  @Test
  public void encodeLenIs0Mod3() {
    assertEquals("Q2F0", Base64Coder.encode(new byte[] {'C', 'a', 't'}));
  }

  @Test
  public void decodeLenIs0Mod3() {
    assertArrayEquals(new byte[]{'C', 'a', 't'}, Base64Coder.decodeBytes("Q2F0"));
  }

  @Test
  public void encodeLenIs1Mod3() {
    assertEquals("Q2F0cw==", Base64Coder.encode(new byte[]{'C', 'a', 't', 's'}));
  }

  @Test
  public void decodeLenIs1Mod3() {
    assertArrayEquals(new byte[]{'C', 'a', 't', 's'}, Base64Coder.decodeBytes("Q2F0cw=="));
  }

  @Test
  public void encodeLenIs2Mod3() {
    assertEquals("QnJhaW4=", Base64Coder.encode(new byte[]{'B', 'r', 'a', 'i', 'n'}));
  }

  @Test
  public void decodeLenIs2Mod3() {
    assertArrayEquals(new byte[]{'B', 'r', 'a', 'i', 'n'}, Base64Coder.decodeBytes("QnJhaW4="));
  }

  @Test
  public void encodeNegativeBytes() {
    assertEquals("////", Base64Coder.encode(new byte[]{-1, -1, -1}));
  }

  @Test
  public void decodeNegativeBytes() {
    assertArrayEquals(new byte[]{-1, -1, -1}, Base64Coder.decodeBytes("////"));
  }

  @Test
  public void negativeDecodeBug() {
    assertArrayEquals(new byte[]{ -1, 0, -1 }, Base64Coder.decodeBytes("/wD/"));
  }

  @Test
  public void encodeOneNegativeByte() {
    assertEquals("/w==", Base64Coder.encode(new byte[]{-1}));
  }

  @Test
  public void decodeOneNegativeByte() {
    assertArrayEquals(new byte[]{-1}, Base64Coder.decodeBytes("/w=="));
  }

  @Test
  public void encodeTwoNegativeBytes() {
    assertEquals("//8=", Base64Coder.encode(new byte[]{-1, -1}));
  }

  @Test
  public void decodeTwoNegativeBytes() {
    assertArrayEquals(new byte[]{-1, -1}, Base64Coder.decodeBytes("//8="));
  }
}