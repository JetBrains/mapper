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
import jetbrains.jetpad.json.JsonBoolean;
import jetbrains.jetpad.json.JsonNull;
import jetbrains.jetpad.json.JsonNumber;
import jetbrains.jetpad.json.JsonObject;
import jetbrains.jetpad.json.JsonString;
import jetbrains.jetpad.json.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class JsonSerializationContext {
  private Map<Byte, Serializer> mySerializersById = new HashMap<Byte, Serializer>();
  private Map<Class, Serializer> mySerializersByClass = new HashMap<Class, Serializer>();

  public JsonSerializationContext() {
    IntegerSerializer integerSerializer = new IntegerSerializer();
    register(Integer.class, integerSerializer);

    StringSerializer stringSerializer = new StringSerializer(integerSerializer);
    register(String.class, stringSerializer);

    register(JsonNull.class, new JsonNullSerializer());
    register(JsonBoolean.class, new JsonBooleanSerializer());

    register(JsonString.class, new JsonStringSerializer(stringSerializer));
    register(JsonNumber.class, new JsonNumberSerializer(integerSerializer, new DoubleSerializer()));

    register(JsonObject.class, new JsonObjectSerializer(this));
    register(JsonArray.class, new JsonArraySerializer(this));
  }

  <T> void register(Class<T> type, Serializer<T> serializer) {
    if (mySerializersByClass.containsKey(type)) throw new IllegalStateException("double register serializer for " + type);

    byte id = serializer.hasId() ? serializer.getId() : (byte) mySerializersByClass.size();
    if (mySerializersById.containsKey(id)) throw new IllegalStateException("duplicate serializer id " + id);

    mySerializersByClass.put(type, serializer);
    mySerializersById.put(id, serializer);
  }

  public JsonValue read(byte[] input) {
    Serializer serializer = getSerializer(input, 0);
    JsonValue value = (JsonValue) serializer.read(input, 1);
    if (serializer.getPosition() != input.length) throw new JsonSerializationException();
    return value;
  }

  public byte[] write(JsonValue value) {
    Serializer serializer = mySerializersByClass.get(value.getClass());
    if (serializer == null) throw new JsonSerializationException("unregistered class " + value.getClass());
    return serializer.write(value);
  }

  Serializer getSerializer(byte[] input, int offset) {
    byte id = input[offset];
    if (!mySerializersById.containsKey(id)) throw new JsonSerializationException("offset=" + offset + ", id=" + id);
    return mySerializersById.get(id);
  }

  <T> Serializer<T> getSerializer(Class<T> type) {
    return mySerializersByClass.get(type);
  }
}
