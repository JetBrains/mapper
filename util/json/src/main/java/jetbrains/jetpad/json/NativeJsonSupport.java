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

import com.google.gwt.core.client.JavaScriptObject;

public class NativeJsonSupport implements JsonSupport {
  @Override
  public JsonValue parse(String input) {
    JavaScriptObject obj = parseJson(input);
    return toJsonValue(obj);
  }

  private native JavaScriptObject parseJson(String json) /*-{
    return $wnd.JSON.parse(json);
  }-*/;

  private static native JsonValue toJsonValue(JavaScriptObject obj) /*-{
    var result;

    if (obj == null) {
      return @jetbrains.jetpad.json.JsonNull::new()();
    }

    if (obj instanceof Array || obj instanceof $wnd.Array) {
      result = @jetbrains.jetpad.json.JsonArray::new()();
      var len = obj.length;
      for (var i = 0; i < len; i++) {
        var curVal = @jetbrains.jetpad.json.NativeJsonSupport::toJsonValue(*)(obj[i]);
        result.@jetbrains.jetpad.json.JsonArray::add(Ljetbrains/jetpad/json/JsonValue;)(curVal);
      }
      return result;
    }

    var type = typeof obj;
    switch (type) {
      case "number":
        return @jetbrains.jetpad.json.JsonNumber::new(D)(obj);
      case "boolean":
        return @jetbrains.jetpad.json.JsonBoolean::new(Z)(obj);
      case "string":
        return @jetbrains.jetpad.json.JsonString::new(Ljava/lang/String;)(obj);
      case "object":
        result = @jetbrains.jetpad.json.JsonObject::new()();
        for (var k in obj) {
          if (obj.hasOwnProperty(k)) {
            var kVal = @jetbrains.jetpad.json.NativeJsonSupport::toJsonValue(*)(obj[k]);
            result.@jetbrains.jetpad.json.JsonObject::put(Ljava/lang/String;Ljetbrains/jetpad/json/JsonValue;)(k, kVal);
          }
        }
        return result;
    }

    return null;
  }-*/;

  @Override
  public String toString(JsonValue value) {
    return value.toString();
  }
}