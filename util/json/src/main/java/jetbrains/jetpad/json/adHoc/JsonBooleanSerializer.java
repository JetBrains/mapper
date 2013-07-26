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

import jetbrains.jetpad.json.JsonBoolean;

class JsonBooleanSerializer extends Serializer<JsonBoolean> {
  JsonBooleanSerializer() {
    super(3);
  }

  @Override
  byte[] write(JsonBoolean data) {
    byte[] buffer = new byte[2];
    buffer[0] = getId();
    buffer[1] = (byte)(data.getBooleanValue() ? 1 : 0);
    setPosition(2);
    return buffer;
  }

  @Override
  JsonBoolean read(byte[] input, int position) {
    setPosition(position);
    byte value = input[getPosition()];
    incPosition();
    if (value == 1) return new JsonBoolean(true);
    if (value == 0) return new JsonBoolean(false);
    throw new JsonSerializationException();
  }
}