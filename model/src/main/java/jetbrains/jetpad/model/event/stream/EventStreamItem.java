/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import jetbrains.jetpad.model.event.ListenerEvent;

public final class EventStreamItem<EventT> implements ListenerEvent<EventStreamListener<EventT>> {
  public static <EventT> EventStreamItem<EventT> event(EventT event) {
    return new EventStreamItem<>(event, null);
  }

  public static <EventT> EventStreamItem<EventT> error(Throwable t) {
    return new EventStreamItem<>(null, t);
  }

  public static <EventT> EventStreamItem<EventT> finalItem() {
    return new EventStreamItem<>(null, null);
  }

  private EventT myEvent;
  private Throwable myError;

  private EventStreamItem(EventT event, Throwable error) {
    if (event != null && error != null) {
      throw new IllegalArgumentException();
    }

    myEvent = event;
    myError = error;
  }

  public EventT getEvent() {
    return myEvent;
  }

  public Throwable getError() {
    return myError;
  }

  public boolean isFinalItem() {
    return myError == null && myEvent == null;
  }

  public boolean isEvent() {
    return myEvent != null;
  }

  public boolean isError() {
    return myError != null;
  }

  @Override
  public void dispatch(EventStreamListener<EventT> l) {
    if (isError()) {
      l.onEvent(myEvent);
    } else if (isError()) {
      l.onError(myError);
    } else {
      l.onEnd();
    }
  }
}