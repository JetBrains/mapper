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
package jetbrains.jetpad.model.id;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class BaseId {
  private static final char SEPARATOR = '$';

  private static final Map<String, String> ourNamesMap = new HashMap<String, String>();

  private static final Random ourRandom = new Random();

  private static long getFirstPart(String s) {
    int index = s.indexOf(SEPARATOR);
    if (index ==  -1) return Coder.decode(s);
    return Coder.decode(s.substring(0, index));
  }

  private static long getSecondPart(String s) {
    int index = s.indexOf(SEPARATOR);
    if (index ==  -1) return 0;
    return Coder.decode(s.substring(index + 1));
  }

  private long myId1;
  private long myId2;
  
  protected BaseId() {
    this(Math.abs(ourRandom.nextLong()), Math.abs(ourRandom.nextLong()), null);
  }

  protected BaseId(String id) {
    this(id, null);
  }

  protected BaseId(String id, String name) {
    this(getFirstPart(id), getSecondPart(id), name);
  }

  private BaseId(long id1, long id2, String name) {
    myId1 = id1;
    myId2 = id2;

    String id = getId();
    synchronized (ourNamesMap) {
      String oldName = ourNamesMap.get(id);

      if (oldName != null && name != null && !oldName.equals(name)) throw new IllegalStateException();

      if (name != null) {
        ourNamesMap.put(id, name);
      }
    }
  }

  public String getId() {
    if (myId2 == 0) return Coder.encode(myId1);
    return Coder.encode(myId1) + SEPARATOR + Coder.encode(myId2);
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

    if (myId1 == otherId.myId1 && myId2 == otherId.myId2) {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    return (int) (myId1 ^ (myId2 << 32));
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
