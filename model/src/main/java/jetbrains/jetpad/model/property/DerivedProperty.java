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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;

/**
 * Simplified version of {@link BaseDerivedProperty} which can depend only on other properties instead of
 * generic observable objects
 */
public abstract class DerivedProperty<ValueT> extends BaseDerivedProperty<ValueT> {
  private Registration[] myRegistrations;
  private ReadableProperty<?>[] myDeps;

  public DerivedProperty(ReadableProperty<?>... deps) {
    this(null, deps);
  }

  public DerivedProperty(ValueT initialValue, ReadableProperty<?>... deps) {
    super(initialValue);
    myDeps = new ReadableProperty<?>[deps.length];
    System.arraycopy(deps, 0, myDeps, 0, deps.length);
  }

  @Override
  protected void doAddListeners() {
    ReadableProperty<?>[] deps = myDeps;
    myRegistrations = new Registration[deps.length];
    for (int i = 0, myDependenciesLength = deps.length; i < myDependenciesLength; i++) {
      myRegistrations[i] = register(deps[i]);
    }
  }

  private <DependencyT> Registration register(ReadableProperty<DependencyT> prop) {
    return prop.addHandler(new EventHandler<PropertyChangeEvent<DependencyT>>() {
      @Override
      public void onEvent(PropertyChangeEvent<DependencyT> event) {
        somethingChanged();
      }
    });
  }

  @Override
  protected void doRemoveListeners() {
    for (Registration r : myRegistrations) {
      r.remove();
    }
    myRegistrations = null;
  }
}