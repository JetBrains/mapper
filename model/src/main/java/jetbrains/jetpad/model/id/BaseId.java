/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import jetbrains.jetpad.base.Objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic id class for typed ids.
 *
 * Supports:
 * - storing a random id
 * - maintaining debug map, so that we can have readable names and efficient representation at the same time
 */
public abstract class BaseId implements Serializable {

  private static final Map<String, String> ourNamesMap = new HashMap<>();

  private String myId;

  // generate 62 ^ 22 (~130 bits) of random readable ID
  protected BaseId() {
    this(IdGenerator.nextBase62RandomId(22), null);
  }

  protected BaseId(String id) {
    this(id, null);
  }

  protected BaseId(String id, String name) {
    myId = id;

    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("id='" + id + "'");
    }

    synchronized (ourNamesMap) {
      String oldName = ourNamesMap.get(id);

      if (oldName != null && name != null && !oldName.equals(name)) {
        throw new IllegalStateException("Duplicate id : [id = " + id + ", oldName = " + oldName + ", name = " + name + "]");
      }

      if (name != null) {
        ourNamesMap.put(id, name);
      }
    }
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

}