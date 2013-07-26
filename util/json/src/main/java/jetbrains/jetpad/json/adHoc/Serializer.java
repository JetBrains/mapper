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
