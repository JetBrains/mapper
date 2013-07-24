package jetbrains.jetpad.json.adHoc;


abstract class ComputedSizeSerializer<T> extends Serializer<T> {
  private static final int DEFAULT_BUFFER_SIZE = 50;
  private static final int MAX_BUFFER_LENGTH = Integer.MAX_VALUE;

  private byte[] myBuffer;
  private Serializer<Integer> myIntSerializer;


  protected abstract void doWrite(T data);
  protected abstract T doRead(byte[] input, int size);

  protected ComputedSizeSerializer(Serializer<Integer> intSerializer) {
    super();
    myIntSerializer = intSerializer;
  }

  protected ComputedSizeSerializer(int id, Serializer<Integer> intSerializer) {
    super(id);
    myIntSerializer = intSerializer;
  }

  @Override
  byte[] write(T data) {
    myBuffer = new byte[DEFAULT_BUFFER_SIZE];
    setPosition(0);

    doWrite(data);
    byte[] size = myIntSerializer.write(getPosition());

    int idByte = hasId() ? 1 : 0;
    int totalSize = idByte + size.length + getPosition();

    byte[] result = new byte[totalSize];
    if (hasId()) {
      result[0] = getId();
    }

    System.arraycopy(size, 0, result, idByte, size.length);
    System.arraycopy(myBuffer, 0, result, idByte + size.length, getPosition());
    setPosition(totalSize);
    return result;
  }

  @Override
  T read(byte[] input, int position) {
    setPosition(position);
    int size = myIntSerializer.read(input, getPosition());
    setPosition(myIntSerializer.getPosition());
    return doRead(input, size);
  }

  protected void write(byte[] bytes) {
    ensureCanWrite(bytes.length);
    System.arraycopy(bytes, 0, myBuffer, getPosition(), bytes.length);
    incPosition(bytes.length);
  }

  protected void write(byte b) {
    ensureCanWrite(1);
    myBuffer[incPosition()] = b;
  }

  protected Serializer<Integer> getIntSerializer() {
    return myIntSerializer;
  }

  private void ensureCanWrite(int size) {
    int deficit = getPosition() + size - myBuffer.length;
    if (deficit <= 0) return;

    int toAdd = Math.max(myBuffer.length, deficit);
    if (toAdd + myBuffer.length > MAX_BUFFER_LENGTH) throw new JsonSerializationException();

    byte[] buffer = new byte[toAdd + myBuffer.length];
    System.arraycopy(myBuffer, 0, buffer, 0, getPosition());

    myBuffer = buffer;
  }
}
