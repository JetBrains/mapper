package jetbrains.jetpad.json;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class NativeJsonSupport implements JsonSupport {
  @Override
  public JsonValue parse(String input) {
    JSONValue jsonValue = JSONParser.parseStrict(input);
    return toJsonValue(jsonValue);
  }

  private JsonValue toJsonValue(JSONValue value) {
    if (value.isNull() != null) {
      return new JsonNull();
    } else if (value.isBoolean() != null) {
      JSONBoolean bool = (JSONBoolean) value;
      return new JsonBoolean(bool.booleanValue());
    } else if (value.isArray() != null) {
      JSONArray array = (JSONArray) value;
      JsonArray result = new JsonArray();
      for (int i = 0; i < array.size(); i++) {
        result.add(toJsonValue(array.get(i)));
      }
      return result;
    } else if (value.isNumber() != null) {
      JSONNumber number = (JSONNumber) value;
      return new JsonNumber(number.doubleValue());
    } else if (value.isObject() != null) {
      JSONObject obj = (JSONObject) value;
      JsonObject result = new JsonObject();
      for (String key : obj.keySet()) {
        result.put(key, toJsonValue(obj.get(key)));
      }
      return result;
    } else if (value.isString() != null) {
      JSONString str = (JSONString) value;
      return new JsonString(str.stringValue());
    }

    throw new IllegalArgumentException();
  }

  @Override
  public String toString(JsonValue value) {
    return value.toString(0);
  }
}
