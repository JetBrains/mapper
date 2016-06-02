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
package jetbrains.jetpad.model.event.stream;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.EventSource;
import jetbrains.jetpad.model.event.EventSources;
import jetbrains.jetpad.model.event.Listeners;

/**
 * This class is inspired by different implementations of observable/event streams, including: Rx, RxJava, bacon.js,
 * Dart Stream API, etc.
 *
 * RxJava doesn't work in GWT, but is used heavily in Java code so class names were chosen to be distinct from
 * classes there to avoid naming conflicts in server side code
 */
public final class EventStream<EventT> {
  private EventSource<EventStreamItem<EventT>> myEventSource;

  public EventStream(EventSource<EventStreamItem<EventT>> eventSource) {
    myEventSource = eventSource;
  }

  public<TargetEventT> EventStream<TargetEventT> map(final Function<? super EventT, ? extends TargetEventT> f) {
    return new EventStream<>(EventSources.map(myEventSource, new Function<EventStreamItem<EventT>, EventStreamItem<TargetEventT>>() {
      @Override
      public EventStreamItem<TargetEventT> apply(EventStreamItem<EventT> input) {
        if (input.isEvent()) {
          return EventStreamItem.<TargetEventT>event(f.apply(input.getEvent()));
        } else if (input.isError()) {
          return EventStreamItem.error(input.getError());
        } else {
          return EventStreamItem.finalItem();
        }
      }
    }));
  }

  public EventStream<EventT> filter(final Predicate<? super EventT> pred) {
    return new EventStream<>(EventSources.filter(myEventSource, new Predicate<EventStreamItem<EventT>>() {
      @Override
      public boolean apply(EventStreamItem<EventT> input) {
        if (input.isEvent()) {
          return pred.apply(input.getEvent());
        } else {
          return true;
        }
      }
    }));
  }

  public Registration addListener(final EventStreamListener<EventT> l) {
    return myEventSource.addHandler(new EventHandler<EventStreamItem<EventT>>() {
      @Override
      public void onEvent(EventStreamItem<EventT> event) {
        if (event.isEvent()) {
          l.onEvent(event.getEvent());
        } else if (event.isError()) {
          l.onError(event.getError());
        } else {
          l.onEnd();
        }
      }
    });
  }
}
