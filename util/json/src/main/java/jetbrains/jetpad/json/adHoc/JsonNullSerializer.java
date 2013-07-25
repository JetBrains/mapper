package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonNull;

class JsonNullSerializer extends Serializer<JsonNull> {
  JsonNullSerializer() {
    super(2);
  }

  @Override
  byte[] write(JsonNull data) {
    byte[] buffer = new byte[1];
    buffer[0] = getId();
    setPosition(1);
    return buffer;
  }

  @Override
  JsonNull read(byte[] input, int position) {
    setPosition(position);
    return new JsonNull();
  }
}
