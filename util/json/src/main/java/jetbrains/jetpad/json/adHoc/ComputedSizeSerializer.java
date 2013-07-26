/*
 * Copyright 2012-2013 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.json.adHoc;

abstract class ComputedSizeSerializer<T> extends Serializer<T> {
  static final int DEFAULT_BUFFER_SIZE = 50;
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
