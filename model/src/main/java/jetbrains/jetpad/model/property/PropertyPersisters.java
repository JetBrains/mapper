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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Persister;

public class PropertyPersisters {
  public static <T> Persister<Property<T>> valuePropertyPersister(final Persister<T> itemPersister) {
    return new Persister<Property<T>>() {
      @Override
      public Property<T> deserialize(String value) {
        if (value == null) {
          return null;
        }
        if (!value.isEmpty() && value.charAt(0) == 'v') {
          return new ValueProperty<>(itemPersister.deserialize(value.substring(1)));
        }
        return new ValueProperty<>();
      }

      @Override
      public String serialize(Property<T> value) {
        if (value == null) {
          return null;
        }
        if (value.get() == null) {
          return "n";
        } else {
          return "v" + itemPersister.serialize(value.get());
        }
      }

      @Override
      public String toString() {
        return "valuePropertyPersister[using = " + itemPersister + "]";
      }
    };
  }
}