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

import jetbrains.jetpad.json.JsonNumber;

class JsonNumberSerializer extends Serializer<JsonNumber> {
  private static final byte INT_ID = 0;
  private static final byte DOUBLE_ID = 1;

  private Serializer<Integer> myIntSerializer;
  private Serializer<Double> myDoubleSerializer;

  JsonNumberSerializer(Serializer<Integer> intSerializer, Serializer<Double> doubleSerializer) {
    super(5);
    myIntSerializer = intSerializer;
    myDoubleSerializer = doubleSerializer;
  }

  @Override
  byte[] write(JsonNumber data) {
    double value = data.getDoubleValue();
    byte[] content;
    byte contentId;
    if (value == (int)value) {
      contentId = INT_ID;
      content = myIntSerializer.write((int) value);
    } else {
      contentId = DOUBLE_ID;
      content = myDoubleSerializer.write(value);
    }
    byte[] buffer = new byte[2 + content.length];
    buffer[0] = getId();
    buffer[1] = contentId;
    System.arraycopy(content, 0, buffer, 2, content.length);
    setPosition(buffer.length);
    return buffer;
  }

  @Override
  JsonNumber read(byte[] input, int position) {
    setPosition(position);
    byte type = input[incPosition()];
    double value;
    if (type == INT_ID) {
      value = myIntSerializer.read(input, getPosition());
      setPosition(myIntSerializer.getPosition());
    } else if (type == DOUBLE_ID) {
      value = myDoubleSerializer.read(input, getPosition());
      setPosition(myDoubleSerializer.getPosition());
    } else {
      throw new JsonSerializationException();
    }
    return new JsonNumber(value);
  }
}