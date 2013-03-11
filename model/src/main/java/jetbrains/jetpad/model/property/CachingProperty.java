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
package jetbrains.jetpad.model.property;

import com.google.common.base.Supplier;

public class CachingProperty<ValueT> extends SimpleDerivedProperty<ValueT> {
  private ReadableProperty<ValueT> myProperty;
  private ValueT myCachedValue;
  private boolean myActive;

  public CachingProperty(final ReadableProperty<ValueT> prop) {
    super(new Supplier<ValueT>() {
      @Override
      public ValueT get() {
        return prop.get();
      }
    }, prop);

    myProperty = prop;
  }

  @Override
  protected void doAddListeners() {
    super.doAddListeners();
    myActive = true;
    myCachedValue = myProperty.get();
  }

  @Override
  protected void doRemoveListeners() {
    myActive = false;
    super.doRemoveListeners();
  }

  @Override
  protected void somethingChanged() {
    myCachedValue = myProperty.get();
    super.somethingChanged();
  }

  @Override
  public ValueT get() {
    if (myActive) {
      return myCachedValue;
    } else {
      return myProperty.get();
    }
  }
}
