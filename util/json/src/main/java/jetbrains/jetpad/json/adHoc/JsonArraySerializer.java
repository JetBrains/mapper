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

import jetbrains.jetpad.json.JsonArray;
import jetbrains.jetpad.json.JsonValue;

class JsonArraySerializer extends RecursiveSerializer<JsonArray> {
  JsonArraySerializer(JsonSerializationContext context) {
    super(7, context);
  }

  @Override
  protected void doWrite(JsonArray data) {
    for (JsonValue value: data) {
      writeValue(value);
    }
  }

  @Override
  protected JsonArray doRead(byte[] input, int size) {
    JsonArray array = new JsonArray();
    int end = getPosition() + size;
    while (getPosition() < end) {
      array.add(readValue(input));
    }
    return array;
  }

  @Override
  protected RecursiveSerializer<JsonArray> createInstance(JsonSerializationContext context) {
    return new JsonArraySerializer(context);
  }
}
