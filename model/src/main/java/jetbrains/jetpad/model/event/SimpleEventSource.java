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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.ThrowableHandlers;

public final class SimpleEventSource<EventT> implements EventSource<EventT> {
  private Listeners<EventHandler<? super EventT>> myListeners = new Listeners<>();

  public void fire(final EventT event) {
    try (Listeners.Firing<EventHandler<? super EventT>> firing = myListeners.fire()) {
      for (EventHandler<? super EventT> l : firing) {
        try {
          l.onEvent(event);
        } catch (Throwable t) {
          ThrowableHandlers.handle(t);
        }
      }
    }
  }

  @Override
  public Registration addHandler(EventHandler<? super EventT> handler) {
    return myListeners.add(handler);
  }
}