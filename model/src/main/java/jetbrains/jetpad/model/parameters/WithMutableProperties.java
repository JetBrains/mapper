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

import com.google.common.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class WithMutableProperties extends WithProperties {

  @Override
  <ValueT> Registration set(final PropertySpec<ValueT> prop, final ValueT old, ValueT value) {
    final PropertyChangeEvent<ValueT> event = new PropertyChangeEvent<>(old, value);

    beforePropertySet(prop, event);

    if (prop.hasDefaultValue() && Objects.equal(value, getDefaultValue(prop))) {
      erase(prop);
    } else {
      put(prop, value);
    }

    afterPropertySet(prop, event);

    return new Registration() {
      @Override
      protected void doRemove() {
        set(prop, old);
      }
    };
  }

  protected <ValueT> void beforePropertySet(PropertySpec<ValueT> prop, PropertyChangeEvent<ValueT> event) {
  }

  protected <ValueT> void afterPropertySet(PropertySpec<ValueT> prop, PropertyChangeEvent<ValueT> event) {
  }
}
