package jetbrains.jetpad.json.adHoc;

abstract class Serializer<T> {
  private int myPosition = 0;
  private final boolean myHasId;
  private final byte myId;

  abstract byte[] write(T data);
  abstract T read(byte[] input, int position);

  protected Serializer() {
    myHasId = false;
    myId = -1;
  }

  protected Serializer(int id) {
    myHasId = true;
    myId = (byte) id;
  }

  protected int getPosition() {
    return myPosition;
  }

  protected void setPosition(int position) {
    myPosition = position;
  }

  protected int incPosition() {
    return myPosition++;
  }

  protected void incPosition(int add) {
    myPosition += add;
  }

  protected boolean hasId() {
    return myHasId;
  }

  protected byte getId() {
    if (!myHasId) throw new UnsupportedOperationException();
    return myId;
  }
}
