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
package jetbrains.jetpad.model.property;

import com.google.common.base.Supplier;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;

public class SimpleDerivedProperty<ValueT> extends BaseDerivedProperty<ValueT> {
  private ReadableProperty<?>[] myDependencies;
  private Registration[] myRegistrations;
  private Supplier<ValueT> mySupplier;

  public SimpleDerivedProperty(Supplier<ValueT> supplier, ReadableProperty<?>... deps) {
    super(supplier.get());
    mySupplier = supplier;

    myDependencies = new ReadableProperty<?>[deps.length];
    System.arraycopy(deps, 0, myDependencies, 0, deps.length);
  }

  @Override
  protected void doAddListeners() {
    myRegistrations = new Registration[myDependencies.length];
    for (int i = 0, myDependenciesLength = myDependencies.length; i < myDependenciesLength; i++) {
      myRegistrations[i] = register(myDependencies[i]);
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

  @Override
  protected ValueT doGet() {
    return mySupplier.get();
  }

  @Override
  public String getPropExpr() {
    StringBuilder result = new StringBuilder();
    result.append("derived(");
    result.append(mySupplier);
    for (ReadableProperty<?> d : myDependencies) {
      result.append(", ");
      result.append(d.getPropExpr());
    }
    result.append(")");
    return result.toString();
  }
}