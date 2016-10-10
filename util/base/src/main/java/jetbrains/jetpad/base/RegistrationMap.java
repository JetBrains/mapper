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
package jetbrains.jetpad.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RegistrationMap<KeyT> {
  private final Map<KeyT, Registration> myMap = new HashMap<>();

  public void put(KeyT key, Registration registration){
    Registration prev = myMap.put(key, registration);
    if (prev != null) {
      prev.remove();
      myMap.remove(key).remove();
      throw new IllegalStateException("Registration for the key '"+key+"' already exists.");
    }
  }

  public boolean replace(KeyT key, Registration registration){
    boolean res = removeOptional(key);
    myMap.put(key, registration);
    return res;
  }

  public void remove(KeyT key) {
    Registration prev = myMap.remove(key);
    if (prev != null) {
      prev.remove();
    } else {
      throw new IllegalStateException("Registration for the key '"+key+"' not found.");
    }
  }

  public boolean removeOptional(KeyT key) {
    Registration prev = myMap.remove(key);
    if (prev != null) {
      prev.remove();
      return true;
    } else {
      return false;
    }
  }

  public Set<KeyT> keys() {
    return myMap.keySet();
  }

  public void clear() {
    try {
      for (Registration r : myMap.values()) {
        r.remove();
      }
    } finally {
      myMap.clear();
    }
  }
}