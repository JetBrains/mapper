package jetbrains.jetpad.json.adHoc;

class DoubleSerializer extends NumericSerializer<Double>{
  DoubleSerializer() {
    super(LONG_MAX_LENGTH, false);
  }

  @Override
  Double read(byte[] input, int position) {
    setPosition(position);
    byte size = isOptimized() ? input[incPosition()] : (byte)getMaxLength();
    long value = 0;
    for (int i = size - 1; i >= 0; i--) {
      if (i == LONG_MAX_LENGTH - 1) {
        value = value | (long)input[incPosition()] << 8 * i;
      } else if (i > INT_MAX_LENGTH - 2) {
        value = value | (long)(input[incPosition()] & 0xFF) << 8 * i;
      } else {
        value = value | (input[incPosition()] & 0xFF) << 8 * i;
      }
    }
    return Double.longBitsToDouble(value);
  }

  @Override
  protected long getLongValue(Double data) {
    return Double.doubleToLongBits(data);
  }

  @Override
  protected byte rightShift(long value, int shift) {
    return (byte)(value >>> shift);
  }
}