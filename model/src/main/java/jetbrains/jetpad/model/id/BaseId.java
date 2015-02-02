/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.model.id;

import com.google.common.base.Objects;
import jetbrains.jetpad.base.base64.Base64URLSafeCoder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class BaseId implements Serializable {
  private static final char SEPARATOR = '.';

  private static final Map<String, String> ourNamesMap = new HashMap<>();

  private static final Random ourRandom = new Random();

  private String myId;

  protected BaseId() {
    this(Math.abs(ourRandom.nextLong()), Math.abs(ourRandom.nextLong()), null);
  }

  protected BaseId(String id) {
    this(id, null);
  }

  protected BaseId(String id, String name) {
    myId = id;
    synchronized (ourNamesMap) {
      String oldName = ourNamesMap.get(id);

      if (oldName != null && name != null && !oldName.equals(name)) {
        throw new IllegalStateException();
      }

      if (name != null) {
        ourNamesMap.put(id, name);
      }
    }
  }

  private BaseId(long id1, long id2, String name) {
    this(getEncodedId(id1, id2), name);
  }

  public String getId() {
    return myId;
  }

  public String getName() {
    synchronized (ourNamesMap) {
      return ourNamesMap.get(getId());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj.getClass() != getClass()) return false;

    BaseId otherId = (BaseId) obj;

    return Objects.equal(getId(), otherId.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public String toString() {
    synchronized (ourNamesMap) {
      String name = ourNamesMap.get(getId());
      if (name != null) {
        return name + " [" + getId() + "]";
      }
      return getId();
    }
  }

  public static String getEncodedId(long id1, long id2) {
    return id2 == 0 ? Base64URLSafeCoder.encode(id1) : Base64URLSafeCoder.encode(id1) + SEPARATOR + Base64URLSafeCoder.encode(id2);
  }
}