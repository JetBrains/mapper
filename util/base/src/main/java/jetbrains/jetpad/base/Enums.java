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
package jetbrains.jetpad.base;

import com.google.common.base.Objects;

public class Enums {
  /**
   * Value of method for enums which takes into account toString() instead of saved generated name
   */
  public static <EnumT extends Enum<EnumT>> EnumT valueOf(Class<EnumT> cls, String name) {
    for (EnumT e : cls.getEnumConstants()) {
      if (Objects.equal(name, e.toString())) {
        return e;
      }
    }

    throw new IllegalArgumentException(name);
  }
}