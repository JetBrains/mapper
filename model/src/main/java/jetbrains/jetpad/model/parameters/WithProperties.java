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
import jetbrains.jetpad.model.util.ListMap;

import java.util.Collections;
import java.util.Set;

abstract class WithProperties {
  private ListMap<PropertySpec<?>, Object> myProperties;

  public final boolean isConfigured(PropertySpec<?> prop) {
    return myProperties != null && myProperties.containsKey(prop);
  }

  public final Set<PropertySpec<?>> getConfiguredProperties() {
    return myProperties == null ? Collections.<PropertySpec<?>>emptySet() : Collections.unmodifiableSet(myProperties.keySet());
  }

  public final <ValueT> ValueT get(PropertySpec<ValueT> prop) {
    if (isConfigured(prop)) {
      return (ValueT) myProperties.get(prop);
    }
    return getDefaultValue(prop);
  }

  protected <ValueT> ValueT getDefaultValue(PropertySpec<ValueT> prop) {
    return prop.getDefaultValue(this);
  }

  protected final <ValueT> ValueT getRaw(PropertySpec<ValueT> prop) {
    if (!isConfigured(prop)) return null;
    return (ValueT) myProperties.get(prop);
  }

  final <ValueT> ValueT erase(PropertySpec<ValueT> prop) {
    if (!isConfigured(prop)) return null;
    ValueT removed = (ValueT) myProperties.remove(prop);
    if (myProperties.isEmpty()) {
      myProperties = null;
    }
    return removed;
  }

  final <ValueT> ValueT put(PropertySpec<ValueT> prop, ValueT value) {
    if (myProperties == null) {
      myProperties = new ListMap<>();
    }
    return (ValueT) myProperties.put(prop, value);
  }

  public final <ValueT> Registration set(final PropertySpec<ValueT> prop, ValueT value) {
    boolean shouldOverwrite;
    final ValueT old;

    if (isConfigured(prop)) {
      old = getRaw(prop);
      shouldOverwrite = !Objects.equal(value, old);
    } else {
      if (prop.hasDefaultValue()) {
        old = getDefaultValue(prop);
        shouldOverwrite = !Objects.equal(value, old);
      } else {
        old = null;
        shouldOverwrite = true;
      }
    }

    if (!shouldOverwrite) return Registration.EMPTY;

    return set(prop, old, value);
  }

  abstract <ValueT> Registration set(PropertySpec<ValueT> prop, ValueT old, ValueT value);
}
