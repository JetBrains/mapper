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
