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

import com.google.common.base.Objects;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.event.Registration;

public class DelayedValueProperty<ValueT> extends BaseReadableProperty<ValueT> implements Property<ValueT> {
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> myHandlers;
  private PropertyChangeEvent<ValueT> myPendingEvent;
  private ValueT myValue;

  public DelayedValueProperty() {
    this(null);
  }

  public DelayedValueProperty(ValueT initialValue) {
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

    if (myPendingEvent != null) throw new IllegalStateException();
    myPendingEvent = new PropertyChangeEvent<ValueT>(oldValue, myValue);
  }

  public void flush() {
    if (myHandlers != null) {
      myHandlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
          l.onEvent(myPendingEvent);
        }
      });
    }
    myPendingEvent = null;
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>>();
    }

    final Registration reg = myHandlers.add(handler);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myHandlers.isEmpty()) {
          myHandlers = null;
        }
      }
    };
  }


  @Override
  public String getPropExpr() {
    return "delayedProperty()";
  }
}