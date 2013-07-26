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

import jetbrains.jetpad.json.JsonObject;

class JsonObjectSerializer extends RecursiveSerializer<JsonObject> {
  private Serializer<String> myStringSerializer;

  JsonObjectSerializer(JsonSerializationContext context) {
    super(6, context);
    myStringSerializer = context.getSerializer(String.class);
  }

  @Override
  protected void doWrite(JsonObject data) {
    for (String key: data.getKeys()) {
      write(myStringSerializer.write(key));
      writeValue(data.get(key));
    }
  }

  @Override
  protected JsonObject doRead(byte[] input, int size) {
    JsonObject result = new JsonObject();
    int end = getPosition() + size;
    while (getPosition() < end) {
      String key = myStringSerializer.read(input, getPosition());
      setPosition(myStringSerializer.getPosition());
      result.put(key, readValue(input));
    }
    return result;
  }

  @Override
  protected RecursiveSerializer<JsonObject> createInstance(JsonSerializationContext context) {
    return new JsonObjectSerializer(context);
  }
}