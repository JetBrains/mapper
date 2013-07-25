package jetbrains.jetpad.json.adHoc;

class StringSerializer extends ComputedSizeSerializer<String> {
  private static final byte UTF8_ID = 0;
  private static final byte UTF16_ID = 1;

  StringSerializer(Serializer<Integer> intSerializer) {
    super(intSerializer);
  }

  @Override
  protected void doWrite(String data) {
    int length = data.length();
    int[] chars = new int[length];
    boolean utf8 = true;
    for (int i = 0; i < length; i++) {
      int k = (int) data.charAt(i);
      chars[i] = k;
      if (k > 255) {
        utf8 = false;
      }
    }
    byte[] result = utf8 ? writeUtf8(chars) : writeUtf16(chars);
    write(result);
  }

  @Override
  protected String doRead(byte[] input, int size) {
    byte encodingId = input[incPosition()];
    char[] content;
    if (encodingId == UTF8_ID) {
      content = readUtf8(input, size - 1);
    } else if (encodingId == UTF16_ID) {
      content = readUtf16(input, size - 1);
    } else {
      throw new JsonSerializationException();
    }
    return new String(content);
  }

  private byte[] writeUtf8(int[] data) {
    byte[] buffer = new byte[data.length + 1];
    buffer[0] = UTF8_ID;
    for (int i = 1; i < buffer.length; i++) {
      buffer[i] = (byte)data[i - 1];
    }
    return buffer;
  }

  private byte[] writeUtf16(int[] data) {
    byte[] buffer = new byte[2 * data.length + 1];
    buffer[0] = UTF16_ID;
    for (int i = 1, j = 0; i < buffer.length; i += 2, j++) {
      buffer[i] = (byte) (data[j] >> 8);
      buffer[i + 1] = (byte) data[j];
    }
    return buffer;
  }

  private char[] readUtf8(byte[] input, int size) {
    char[] buffer = new char[size];
    for (int i = 0; i < size; i++) {
      buffer[i] = (char)input[incPosition()];
    }
    return buffer;
  }

  private char[] readUtf16(byte[] input, int size) {
    if (size % 2 != 0) throw new JsonSerializationException();
    int length = size / 2;
    char[] buffer = new char[length];
    for (int i = 0; i < length; i++) {
      buffer[i] = (char) ((input[incPosition()] & 0xFF) << 8 | (input[incPosition()] & 0xFF));
    }
    return buffer;
  }
}