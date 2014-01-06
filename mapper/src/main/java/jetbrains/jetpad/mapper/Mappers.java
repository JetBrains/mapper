/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.mapper;

public class Mappers {
  public static boolean isDescendant(Mapper<?, ?> ancestor, Mapper<?, ?> descendant) {
    if (descendant == ancestor) return true;
    Mapper<?, ?> parent = descendant.getParent();
    if (parent == null) return false;
    return isDescendant(ancestor, parent);
  }

  public static Mapper<?, ?> getRoot(Mapper<?, ?> mapper) {
    Mapper<?, ?> parent = mapper.getParent();
    if (parent == null) return mapper;
    return getRoot(parent);
  }
}