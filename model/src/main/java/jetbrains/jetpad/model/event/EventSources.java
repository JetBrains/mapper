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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;

import java.util.ArrayList;
import java.util.List;

public final class EventSources {
  /**
   * Event source which always dispatched the same events on subscription. It's useful for testing and
   * composition. In Rx-like libraries a similar thing is called cold observable.
   */
  @SafeVarargs
  public static <EventT> EventSource<EventT> of(final EventT... events) {
    return new EventSource<EventT>() {
      @Override
      public Registration addHandler(EventHandler<? super EventT> handler) {
        for (EventT e : events) {
          handler.onEvent(e);
        }
        return Registration.EMPTY;
      }
    };
  }

  public static <EventT> EventSource<EventT> empty() {
    return composite();
  }

  @SafeVarargs
  public static <EventT> EventSource<EventT> composite(EventSource<? extends EventT>... sources) {
    return new CompositeEventSource<>(sources);
  }


  public static <EventT> EventSource<EventT> composite(Iterable<? extends EventSource<? extends EventT>> sources) {
    return new CompositeEventSource<>(sources);
  }

  public static <EventT> EventSource<EventT> filter(final EventSource<EventT> source, final Predicate<? super EventT> pred) {
    return new EventSource<EventT>() {
      @Override
      public Registration addHandler(final EventHandler<? super EventT> handler) {
        return source.addHandler(new EventHandler<EventT>() {
          @Override
          public void onEvent(EventT event) {
            if (pred.test(event)) {
              handler.onEvent(event);
            }
          }
        });
      }
    };
  }

  public static <SourceEventT, TargetEventT> EventSource<TargetEventT> map(EventSource<SourceEventT> src, Function<SourceEventT, TargetEventT> f) {
    return new MappingEventSource<>(src, f);
  }

  public static <EventT, ItemT> EventSource<EventT> selectList(
      final ObservableList<ItemT> list, final Function<ItemT, EventSource<? extends EventT>> selector) {
    return new EventSource<EventT>() {
      @Override
      public Registration addHandler(final EventHandler<? super EventT> handler) {
        final List<Registration> itemRegs = new ArrayList<>();
        for (ItemT item : list) {
          itemRegs.add(selector.apply(item).addHandler(handler));
        }


        final Registration listReg = list.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            itemRegs.add(event.getIndex(), selector.apply(event.getNewItem()).addHandler(handler));
          }

          @Override
          public void onItemRemoved(CollectionItemEvent<? extends ItemT> event) {
            itemRegs.remove(event.getIndex()).remove();
          }
        });

        return new Registration() {
          @Override
          protected void doRemove() {
            for (Registration r : itemRegs) {
              r.remove();
            }

            listReg.remove();
          }
        };
      }
    };
  }

  private EventSources() {
  }
}
