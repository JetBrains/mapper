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
package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.event.EventHandler;

public class UnmodifiableObservableList<ElementT> extends UnmodifiableList<ElementT> implements ObservableList<ElementT> {
  public UnmodifiableObservableList(ObservableList<ElementT> wrappedList) {
    super(wrappedList);
  }

  @Override
  protected ObservableList<ElementT> getWrappedList() {
    return (ObservableList<ElementT>) super.getWrappedList();
  }

  @Override
  public Registration addListener(CollectionListener<? super ElementT> l) {
    return getWrappedList().addListener(l);
  }

  @Override
  public Registration addHandler(EventHandler<? super CollectionItemEvent<? extends ElementT>> handler) {
    return getWrappedList().addHandler(handler);
  }
}