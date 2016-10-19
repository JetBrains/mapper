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
package jetbrains.jetpad.model.collections.list;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;

import jetbrains.jetpad.base.function.Function;

abstract class SelectedCollection<ValueT, ItemT, CollectionT extends ObservableCollection<?>>
  extends ObservableArrayList<ItemT> implements EventHandler<PropertyChangeEvent<ValueT>> {
  private final ReadableProperty<ValueT> mySource;
  private final Function<ValueT, CollectionT> mySelector;

  private Registration mySourcePropertyRegistration = Registration.EMPTY;
  private Registration mySourceListRegistration = Registration.EMPTY;

  private boolean myFollowing = false;

  protected SelectedCollection(ReadableProperty<ValueT> source, Function<ValueT, CollectionT> selector) {
    mySource = source;
    mySelector = selector;
  }

  protected abstract Registration follow(CollectionT collectionT);

  protected abstract CollectionT empty();

  protected boolean isFollowing() {
    return myFollowing;
  }

  protected CollectionT select() {
    ValueT sourceVal = mySource.get();
    if (sourceVal != null) {
      CollectionT res = mySelector.apply(sourceVal);
      if (res != null) return res;
    }

    return empty();
  }

  @Override
  public void onEvent(PropertyChangeEvent<ValueT> event) {
    if (event.getOldValue() != null) {
      clear();
    }

    mySourceListRegistration.remove();
    mySourceListRegistration = follow(select());
  }

  @Override
  protected void onListenersAdded() {
    mySourcePropertyRegistration = mySource.addHandler(this);
    myFollowing = true;
    mySourceListRegistration = follow(select());
  }

  @Override
  protected void onListenersRemoved() {
    mySourcePropertyRegistration.remove();
    myFollowing = false;
    mySourceListRegistration.remove();
  }

  @Override
  public int size() {
    if (isFollowing()) {
      return super.size();
    } else {
      return select().size();
    }
  }
}