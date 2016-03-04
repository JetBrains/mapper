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
package jetbrains.jetpad.model.parameters;

import com.google.common.base.Function;

/**
 * Specification for a property.
 */
public class PropertySpec<ValueT> {

  public static <ValueT> PropertySpec<ValueT> withoutDefaultValue(String name) {
    return new PropertySpec<>(name, null);
  }

  public static <ValueT> PropertySpec<ValueT> of(String name) {
    return of(name, (ValueT) null);
  }

  public static <ValueT> PropertySpec<ValueT> of(String name, final ValueT defaultValue) {
    return new PropertySpec<>(name, new Function<HasProperties, ValueT>() {
      @Override
      public ValueT apply(HasProperties model) {
        return defaultValue;
      }
    });
  }

  public static <ValueT> PropertySpec<ValueT> of(String name, Function<HasProperties, ValueT> defaultValue) {
    return new PropertySpec<>(name, defaultValue);
  }

  private final String myName;
  private final Function<HasProperties, ValueT> myDefaultValue;

  protected PropertySpec(String name, Function<HasProperties, ValueT> defaultValue) {
    if (name == null) {
      throw new IllegalArgumentException();
    }
    myName = name;
    myDefaultValue = defaultValue;
  }

  public ValueT getDefaultValue(HasProperties model) {
    if (myDefaultValue == null) {
      throw new IllegalStateException("Undefined default value for property '" + this + '\'');
    }
    return myDefaultValue.apply(model);
  }

  public boolean hasDefaultValue() {
    return myDefaultValue != null;
  }

  @Override
  public String toString() {
    return myName;
  }
}
