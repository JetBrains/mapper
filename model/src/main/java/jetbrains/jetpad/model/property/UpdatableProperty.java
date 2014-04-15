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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import java.util.Objects;

public abstract class UpdatableProperty<ValueT> implements ReadableProperty<ValueT> {
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> myListeners = new Listeners<>();
  private ValueT myValue;

  protected abstract ValueT doGet();

  protected void doStartPolling() {
  }

  protected void doStopPolling() {
  }

  @Override
  public ValueT get() {
    if (myListeners.isEmpty()) {
      return doGet();
    } else {
      return myValue;
    }
  }


  @Override
  public String getPropExpr() {
    return "updatable property";
  }

  private void startPolling() {
    update(false);
    doStartPolling();
  }

  private void stopPolling() {
    doStopPolling();
  }

  public void update() {
    update(true);
  }

  protected void update(boolean fire) {
    ValueT oldValue = myValue;
    myValue = doGet();
    if (fire && !Objects.equals(oldValue, myValue)) {
      final PropertyChangeEvent<ValueT> change = new PropertyChangeEvent<>(oldValue, myValue);
      myListeners.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
          l.onEvent(change);
        }
      });
    }
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    if (myListeners.isEmpty()) {
      startPolling();
    }
    final Registration reg = myListeners.add(handler);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myListeners.isEmpty()) {
          stopPolling();
        }
      }
    };
  }
}