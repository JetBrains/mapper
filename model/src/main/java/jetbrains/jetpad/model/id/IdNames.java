/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class IdNames {
  private final ConcurrentMap<String, String> myMap = new ConcurrentHashMap<>();

  /**
   * @throws IllegalStateException if the id already has another name.
   */
  void save(String id, String name) {
    String oldName = myMap.putIfAbsent(id, name);
    if (oldName != null && !oldName.equals(name)) {
      throw new IllegalStateException(
          "Different name for known id " + id + ", first name = " + oldName + ", name = " + name + "]");
    }
  }

  String get(String id) {
    return myMap.get(id);
  }
}