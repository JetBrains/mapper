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
package jetbrains.jetpad.model.collections.set;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;

public class UnmodifiableObservableSet<ElementT> extends UnmodifiableSet<ElementT> implements ObservableSet<ElementT> {
  public UnmodifiableObservableSet(ObservableSet<ElementT> wrappedSet) {
    super(wrappedSet);
  }

  @Override
  protected ObservableSet<ElementT> getWrappedSet() {
    return (ObservableSet<ElementT>) super.getWrappedSet();
  }

  @Override
  public Registration addListener(CollectionListener<ElementT> l) {
    return getWrappedSet().addListener(l);
  }

  @Override
  public Registration addHandler(EventHandler<? super CollectionItemEvent<? extends ElementT>> handler) {
    return getWrappedSet().addHandler(handler);
  }


}