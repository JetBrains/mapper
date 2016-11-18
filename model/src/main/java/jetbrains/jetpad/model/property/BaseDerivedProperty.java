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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Objects;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

/**
 * Base class for creation of derived properties, i.e. properties whose values are calculated based on other values
  */
public abstract class BaseDerivedProperty<ValueT> extends BaseReadableProperty<ValueT> {
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> myHandlers;
  private ValueT myValue;

  protected BaseDerivedProperty(ValueT initialValue) {
    myValue = initialValue;
  }

  /**
   * Start listening to the objects which our value depend on
   */
  protected abstract void doAddListeners();

  /**
   * Stop listening to the objects which our value depende on
   */
  protected abstract void doRemoveListeners();

  /**
   * Calculates dependent value
   */
  protected abstract ValueT doGet();

  @Override
  public final ValueT get() {
    if (myHandlers != null) {
      return myValue;
    } else {
      return doGet();
    }
  }

  protected void somethingChanged() {
    ValueT newValue = doGet();
    if (Objects.equal(myValue, newValue)) return;

    final PropertyChangeEvent<ValueT> event = new PropertyChangeEvent<>(myValue, newValue);
    myValue = newValue;

    if (myHandlers != null) {
      try (Listeners.Firing<EventHandler<? super PropertyChangeEvent<ValueT>>> firing = myHandlers.fire()) {
        for (EventHandler<? super PropertyChangeEvent<ValueT>> l : firing) {
          try {
            l.onEvent(event);
          } catch (Throwable t) {
            ThrowableHandlers.handle(t);
          }
        }
      }
    }
  }

  @Override
  public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
        @Override
        protected void beforeFirstAdded() {
          myValue = doGet();
          doAddListeners();
        }

        @Override
        protected void afterLastRemoved() {
          doRemoveListeners();
          myHandlers = null;
        }
      };
    }
    return myHandlers.add(handler);
  }
}