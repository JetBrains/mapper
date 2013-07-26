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

import jetbrains.jetpad.json.JsonValue;

abstract class RecursiveSerializer<T> extends ComputedSizeSerializer<T> {
  private final JsonSerializationContext myContext;

  protected RecursiveSerializer(int id, JsonSerializationContext context) {
    super(id, context.getSerializer(Integer.class));
    myContext = context;
  }

  protected void writeValue(JsonValue value) {
    Serializer serializer = myContext.getSerializer(value.getClass());
    if (serializer instanceof RecursiveSerializer) {
      serializer = ((RecursiveSerializer) serializer).createInstance(myContext);
    }
    write(serializer.write(value));
  }

  protected JsonValue readValue(byte[] input) {
    Serializer serializer = myContext.getSerializer(input, getPosition());
    JsonValue value = (JsonValue) serializer.read(input, getPosition() + 1);
    setPosition(serializer.getPosition());
    return value;
  }

  protected abstract RecursiveSerializer<T> createInstance(JsonSerializationContext context);
}
