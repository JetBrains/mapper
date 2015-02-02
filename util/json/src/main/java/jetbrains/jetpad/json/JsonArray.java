/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import java.util.*;

public class JsonArray extends JsonValue implements Iterable<JsonValue> {
  private List<JsonValue> myValues;

  public JsonArray() {
  }

  public void add(JsonValue value) {
    add(size(), value);
  }

  public void add(int pos, JsonValue value) {
    if (myValues == null) {
      myValues = new ArrayList<>(1);
    }
    myValues.add(pos, value);
  }

  public void remove(int pos) {
    myValues.remove(pos);
  }

  public void add(String s) {
    add(new JsonString(s));
  }

  public void add(double d) {
    add(new JsonNumber(d));
  }

  public void add(boolean b) {
    add(new JsonBoolean(b));
  }

  public int size() {
    if (myValues == null) {
      return 0;
    }
    return myValues.size();
  }

  public JsonValue get(int index) {
    if (myValues == null) {
      throw new IndexOutOfBoundsException();
    }
    return myValues.get(index);
  }

  public JsonObject getObject(int index) {
    return (JsonObject) get(index);
  }

  public JsonArray getArray(int index) {
    return (JsonArray) get(index);
  }

  public String getString(int index) {
    return ((JsonString) get(index)).getStringValue();
  }

  public double getDouble(int index) {
    return ((JsonNumber) get(index)).getDoubleValue();
  }

  public boolean getBoolean(int index) {
    return ((JsonBoolean) get(index)).getBooleanValue();
  }

  public int getInt(int index) {
    return (int) getDouble(index);
  }

  public long getLong(int index) {
    return (long) getDouble(index);
  }

  protected void toString(IndentBuilder builder) {
    builder.append("[");
    builder.indent();
    builder.newLine();
    for (int i = 0; i < size(); i++) {
      get(i).toString(builder);
      if (i != size() - 1) {
        builder.append(",");
        builder.newLine();
      }
    }
    builder.unindent();
    builder.newLine();
    builder.append("]");
  }

  public Iterator<JsonValue> iterator() {
    if (myValues == null) {
      return Arrays.asList(new JsonValue[0]).iterator();
    }
    return Collections.unmodifiableList(myValues).iterator();
  }
}