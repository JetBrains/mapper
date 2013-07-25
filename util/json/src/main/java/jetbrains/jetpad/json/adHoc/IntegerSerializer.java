package jetbrains.jetpad.json.adHoc;

class IntegerSerializer extends NumericSerializer<Integer> {
  IntegerSerializer() {
    super(INT_MAX_LENGTH, true);
  }

  @Override
  Integer read(byte[] input, int position) {
    setPosition(position);
    byte size = isOptimized() ? input[incPosition()] : (byte)getMaxLength();
    int value = 0;
    for (int i = size - 1; i >= 0; i--) {
      value = value | (input[incPosition()] & 0xFF) << 8 * i;
    }
    return value;
  }

  @Override
  protected long getLongValue(Integer data) {
    return (long)data;
  }

  @Override
  protected byte rightShift(long value, int shift) {
    return (byte)(value >> shift);
  }
}
