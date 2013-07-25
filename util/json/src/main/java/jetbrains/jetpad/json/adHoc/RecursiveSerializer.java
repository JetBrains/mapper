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
