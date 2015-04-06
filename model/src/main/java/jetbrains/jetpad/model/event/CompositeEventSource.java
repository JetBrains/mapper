/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import java.util.ArrayList;
import java.util.List;

final class CompositeEventSource<EventT> implements EventSource<EventT> {
  private Listeners<EventHandler<? super EventT>> myHandlers = new Listeners<>();
  private List<EventSource<? extends EventT>> myEventSources = new ArrayList<>();
  private List<Registration> myRegistrations = new ArrayList<>();

  CompositeEventSource(EventSource<? extends EventT>... sources) {
    for (EventSource<? extends EventT> s : sources) {
      add(s);
    }
  }

  void add(EventSource<? extends EventT> source) {
    myEventSources.add(source);
  }

  void remove(EventSource<? extends EventT> source) {
    myEventSources.remove(source);
  }

  @Override
  public Registration addHandler(final EventHandler<? super EventT> handler) {
    if (myHandlers.isEmpty()) {
      for (EventSource<? extends EventT> src : myEventSources) {
        addHandlerTo(src);
      }
    }

    final Registration reg = myHandlers.add(handler);
    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myHandlers.isEmpty()) {
          for (Registration hr : myRegistrations) {
            hr.remove();
          }
          myRegistrations.clear();
        }
      }
    };
  }

  private <PartEventT extends EventT> void addHandlerTo(EventSource<PartEventT> src) {
    myRegistrations.add(src.addHandler(new EventHandler<PartEventT>() {
      @Override
      public void onEvent(final PartEventT event) {
        myHandlers.fire(new ListenerCaller<EventHandler<? super EventT>>() {
          @Override
          public void call(EventHandler<? super EventT> item) {
            item.onEvent(event);
          }
        });
      }
    }));
  }
}