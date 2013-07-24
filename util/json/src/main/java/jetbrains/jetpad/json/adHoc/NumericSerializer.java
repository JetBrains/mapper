package jetbrains.jetpad.json.adHoc;

abstract class NumericSerializer<T> extends Serializer<T> {
  protected static final int INT_MAX_LENGTH = 4;
  protected static final int LONG_MAX_LENGTH = 8;
  private final int myMaxLength;
  private final boolean myOptimize;

  protected NumericSerializer(int maxLength, boolean optimize) {
    myMaxLength = maxLength;
    myOptimize = optimize;
  }

  protected abstract long getLongValue(T data);
  protected abstract byte rightShift(long value, int sift);

  @Override
  byte[] write(T data) {
    int sizeByte = myOptimize ? 1 : 0;
    byte[] buffer = new byte[sizeByte + myMaxLength];
    setPosition(sizeByte);

    boolean started = false;
    long value = getLongValue(data);
    for (int i = 1; i <= myMaxLength; i++) {
      byte b = rightShift(value, 8 * (myMaxLength - i));
      if (!myOptimize || started || b != 0 || i == myMaxLength) {
        started = true;
        buffer[incPosition()] = b;
      }
    }

    if (myOptimize) {
      buffer[0] = (byte) (getPosition() - 1);
    }

    if (getPosition() == buffer.length) return buffer;
    byte[] result = new byte[getPosition()];
    System.arraycopy(buffer, 0, result, 0, getPosition());
    return result;
  }

  protected int getMaxLength() {
    return myMaxLength;
  }

  protected boolean isOptimized() {
    return myOptimize;
  }
}
