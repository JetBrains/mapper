package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonString;

class JsonStringSerializer extends Serializer<JsonString> {
  private Serializer<String> myStringSerializer;

  JsonStringSerializer(Serializer<String> stringSerializer) {
    super(4);
    myStringSerializer = stringSerializer;
  }

  @Override
  byte[] write(JsonString data) {
    byte[] value = myStringSerializer.write(data.getStringValue());
    byte[] result = new byte[value.length + 1];
    result[0] = getId();
    System.arraycopy(value, 0, result, 1, value.length);
    return result;
  }

  @Override
  JsonString read(byte[] input, int position) {
    String s = myStringSerializer.read(input, position);
    setPosition(myStringSerializer.getPosition());
    return new JsonString(s);
  }
}