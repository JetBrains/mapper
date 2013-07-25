package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonArray;
import jetbrains.jetpad.json.JsonValue;

public class JsonArraySerializer extends RecursiveSerializer<JsonArray> {
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
