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
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;

/**
 * One and two-way property binding support
 */
public final class PropertyBinding {
  public static <ValueT> Registration bindOneWay(
      ReadableProperty<? extends ValueT> source, final WritableProperty<? super ValueT> target) {
    target.set(source.get());
    return source.addHandler(new EventHandler<PropertyChangeEvent<? extends ValueT>>() {
      @Override
      public void onEvent(PropertyChangeEvent<? extends ValueT> event) {
        target.set(event.getNewValue());
      }
    });
  }

  public static <ValueT> Registration bindTwoWay(final Property<ValueT> source, final Property<ValueT> target) {
    final Property<Boolean> syncing = new ValueProperty<>(false);
    target.set(source.get());

    class UpdatingEventHandler implements EventHandler<PropertyChangeEvent<ValueT>> {
      private boolean myForward;

      UpdatingEventHandler(boolean forward) {
        myForward = forward;
      }

      @Override
      public void onEvent(PropertyChangeEvent<ValueT> event) {
        if (syncing.get()) return;

        syncing.set(true);
        try {
          if (myForward) {
            target.set(source.get());
          } else {
            source.set(target.get());
          }
        } finally {
          syncing.set(false);
        }
      }
    }

    return new CompositeRegistration(
      source.addHandler(new UpdatingEventHandler(true)),
      target.addHandler(new UpdatingEventHandler(false))
    );
  }

  private PropertyBinding() {
  }
}