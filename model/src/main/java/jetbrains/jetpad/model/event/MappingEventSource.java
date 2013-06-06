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

import com.google.common.base.Function;

public class MappingEventSource<SourceEventT, TargetEventT> implements EventSource<TargetEventT> {
  private Listeners<EventHandler<? super TargetEventT>> myHandlers = new Listeners<EventHandler<? super TargetEventT>>();
  private EventSource<SourceEventT> mySourceEventSource;
  private Function<SourceEventT, TargetEventT> myFunction;

  private Registration mySourceHandler;

  public MappingEventSource(EventSource<SourceEventT> sourceEventSource, Function<SourceEventT, TargetEventT> function) {
    mySourceEventSource = sourceEventSource;
    myFunction = function;
  }

  @Override
  public Registration addHandler(final EventHandler<? super TargetEventT> handler) {
    if (myHandlers.isEmpty()) {
      mySourceHandler = mySourceEventSource.addHandler(new EventHandler<SourceEventT>() {
        @Override
        public void onEvent(SourceEventT item) {
          final TargetEventT targetEvent = myFunction.apply(item);
          myHandlers.fire(new ListenerCaller<EventHandler<? super TargetEventT>>() {
            @Override
            public void call(EventHandler<? super TargetEventT> item) {
              item.onEvent(targetEvent);
            }
          });
        }
      });
    }
    final Registration reg = myHandlers.add(handler);

    return new Registration() {
      @Override
      public void remove() {
        reg.remove();
        if (myHandlers.isEmpty()) {
          mySourceHandler.remove();
        }
      }
    };
  }
}
