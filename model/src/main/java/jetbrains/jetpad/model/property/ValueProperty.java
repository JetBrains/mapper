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
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

/**
 * A simple implementation of Read/Write property which stores the value in a field
 */
public class ValueProperty<ValueT> extends BaseReadableProperty<ValueT> implements Property<ValueT> {
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> myHandlers;
  private ValueT myValue;

  public ValueProperty() {
    this(null);
  }

  public ValueProperty(ValueT initialValue) {
    myValue = initialValue;
  }

  @Override
  public ValueT get() {
    return myValue;
  }

  @Override
  public void set(ValueT value) {
    if (Objects.equal(value, myValue)) return;
    ValueT oldValue = myValue;
    myValue = value;

    fireEvents(oldValue, myValue);
  }

  protected void fireEvents(final ValueT oldValue, final ValueT newValue) {
    if (myHandlers != null) {
      final PropertyChangeEvent<ValueT> event = new PropertyChangeEvent<>(oldValue, newValue);
      myHandlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
          l.onEvent(event);
        }
      });
    }
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
        @Override
        protected void afterLastRemoved() {
          myHandlers = null;
        }
      };
    }

    return myHandlers.add(handler);
  }

  @Override
  public String getPropExpr() {
    return "valueProperty()";
  }
}