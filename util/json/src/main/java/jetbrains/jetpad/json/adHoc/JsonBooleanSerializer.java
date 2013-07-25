package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonBoolean;

class JsonBooleanSerializer extends Serializer<JsonBoolean> {
  JsonBooleanSerializer() {
    super(3);
  }

  @Override
  byte[] write(JsonBoolean data) {
    byte[] buffer = new byte[2];
    buffer[0] = getId();
    buffer[1] = (byte)(data.getBooleanValue() ? 1 : 0);
    setPosition(2);
    return buffer;
  }

  @Override
  JsonBoolean read(byte[] input, int position) {
    setPosition(position);
    byte value = input[getPosition()];
    incPosition();
    if (value == 1) return new JsonBoolean(true);
    if (value == 0) return new JsonBoolean(false);
    throw new JsonSerializationException();
  }
}