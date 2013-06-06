/*
 * Copyright 2012-2013 JetBrains s.r.o
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

public class SimpleEventSource<EventT> implements EventSource<EventT> {
  private Listeners<EventHandler<? super EventT>> myListeners = new Listeners<EventHandler<? super EventT>>();

  public void fire(final EventT event) {
    myListeners.fire(new ListenerCaller<EventHandler<? super EventT>>() {
      @Override
      public void call(EventHandler<? super EventT> l) {
        l.onEvent(event);
      }
    });
  }

  @Override
  public Registration addHandler(EventHandler<? super EventT> handler) {
    return myListeners.add(handler);
  }
}
