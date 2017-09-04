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
package jetbrains.jetpad.json;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonObject extends JsonValue {
  private Map<String, JsonValue> myValues = new LinkedHashMap<>();

  public JsonObject() {
  }

  public void put(String key, @Nonnull JsonValue value) {
    myValues.put(key, value);
  }

  public void put(String key, String value) {
    if (value != null) {
      put(key, new JsonString(value));
    }
  }

  public void put(String key, double value) {
    put(key, new JsonNumber(value));
  }

  public void put(String key, boolean value) {
    put(key, new JsonBoolean(value));
  }

  public JsonValue get(String key) {
    return myValues.get(key);
  }

  public void remove(String key) {
    myValues.remove(key);
  }

  public String getString(String key) {
    JsonString value = (JsonString) get(key);
    if (value == null) {
      return null;
    }
    return value.getStringValue();
  }

  public int getInt(String key) {
    JsonNumber value = (JsonNumber) get(key);
    if (value == null) {
      return 0;
    }
    return value.getIntValue();
  }

  public double getDouble(String key) {
    JsonNumber value = (JsonNumber) get(key);
    if (value == null) {
      return 0;
    }
    return value.getDoubleValue();
  }

  public long getLong(String key) {
    JsonNumber value = (JsonNumber) get(key);
    if (value == null) {
      return 0;
    }
    return value.getLongValue();
  }

  public boolean getBoolean(String key) {
    JsonBoolean value = (JsonBoolean) get(key);
    if (value == null) {
      return false;
    }
    return value.getBooleanValue();
  }

  public JsonObject getObject(String key) {
    return (JsonObject) get(key);
  }

  public JsonArray getArray(String key) {
    return (JsonArray) get(key);
  }

  public Set<String> getKeys() {
    return Collections.unmodifiableSet(myValues.keySet());
  }

  @Override
  protected void toString(IndentBuilder builder) {
    builder.append("{");
    builder.indent();
    builder.newLine();
    List<String> keys = new ArrayList<>(myValues.keySet());
    for (int i = 0; i < keys.size(); i++) {
      builder.append("\"").append(JsonUtil.escape(keys.get(i))).append("\":");
      myValues.get(keys.get(i)).toString(builder);
      if (i != keys.size() - 1) {
        builder.append(",");
        builder.newLine();
      }
    }
    builder.unindent();
    builder.newLine();
    builder.append("}");
  }
}