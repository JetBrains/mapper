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
    if (obj == null) {
      return @jetbrains.jetpad.json.JsonNull::new()();
    }

    if (obj instanceof Array || obj instanceof $wnd.Array) {
      var arr = @jetbrains.jetpad.json.JsonArray::new()();
      var len = obj.length;
      for (var i = 0; i < len; i++) {
        var curVal = @jetbrains.jetpad.json.NativeJsonSupport::toJsonValue(*)(obj[i]);
        arr.@jetbrains.jetpad.json.JsonArray::add(Ljetbrains/jetpad/json/JsonValue;)(curVal);
      }
      return arr;
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
        var o = @jetbrains.jetpad.json.JsonObject::new()();
        for (var k in obj) {
          if (obj.hasOwnProperty(k)) {
            var kVal = @jetbrains.jetpad.json.NativeJsonSupport::toJsonValue(*)(obj[k]);
            o.@jetbrains.jetpad.json.JsonObject::put(Ljava/lang/String;Ljetbrains/jetpad/json/JsonValue;)(k, kVal);
          }
        }
        return o;
    }

    return null;
  }-*/;

  @Override
  public String toString(JsonValue value) {
    return value.toString();
  }
}
