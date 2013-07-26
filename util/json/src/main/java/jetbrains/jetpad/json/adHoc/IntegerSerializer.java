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
package jetbrains.jetpad.json.adHoc;

class IntegerSerializer extends NumericSerializer<Integer> {
  IntegerSerializer() {
    super(INT_MAX_LENGTH, true);
  }

  @Override
  Integer read(byte[] input, int position) {
    setPosition(position);
    byte size = isOptimized() ? input[incPosition()] : (byte)getMaxLength();
    int value = 0;
    for (int i = size - 1; i >= 0; i--) {
      value = value | (input[incPosition()] & 0xFF) << 8 * i;
    }
    return value;
  }

  @Override
  protected long getLongValue(Integer data) {
    return (long)data;
  }

  @Override
  protected byte rightShift(long value, int shift) {
    return (byte)(value >> shift);
  }
}
