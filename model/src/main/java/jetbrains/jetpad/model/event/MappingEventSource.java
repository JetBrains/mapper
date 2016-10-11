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

import java.util.function.Function;

final class MappingEventSource<SourceEventT, TargetEventT> implements EventSource<TargetEventT> {
  private EventSource<SourceEventT> mySourceEventSource;
  private Function<SourceEventT, TargetEventT> myFunction;

  MappingEventSource(EventSource<SourceEventT> sourceEventSource, Function<SourceEventT, TargetEventT> function) {
    mySourceEventSource = sourceEventSource;
    myFunction = function;
  }

  @Override
  public Registration addHandler(final EventHandler<? super TargetEventT> handler) {
    return mySourceEventSource.addHandler(new EventHandler<SourceEventT>() {
      @Override
      public void onEvent(SourceEventT event) {
        handler.onEvent(myFunction.apply(event));
      }
    });
  }
}
