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

import jetbrains.jetpad.json.JsonNull;

class JsonNullSerializer extends Serializer<JsonNull> {
  JsonNullSerializer() {
    super(2);
  }

  @Override
  byte[] write(JsonNull data) {
    byte[] buffer = new byte[1];
    buffer[0] = getId();
    setPosition(1);
    return buffer;
  }

  @Override
  JsonNull read(byte[] input, int position) {
    setPosition(position);
    return new JsonNull();
  }
}
