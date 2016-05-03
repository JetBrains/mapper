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

import com.google.common.base.Function;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Selector;

import java.util.ArrayList;
import java.util.List;

public class EventSources {
  /**
   * Joins several {@link EventSource}s into one
   */
  @SafeVarargs
  public static <EventT> EventSource<EventT> composite(EventSource<? extends EventT>... sources) {
    return new CompositeEventSource<>(sources);
  }

  /**
   * Maps one type of event source to another
   */
  public static <SourceEventT, TargetEventT> EventSource<TargetEventT> map(EventSource<SourceEventT> src, Function<SourceEventT, TargetEventT> f) {
    return new MappingEventSource<>(src, f);
  }

  /**
   * Joins observable list of {@link EventSource}s into one {@link EventSource}
   */
  public static <EventT, ItemT> EventSource<EventT> selectList(final ObservableList<ItemT> list, final Selector<ItemT, EventSource<? extends EventT>> selector) {
    return new EventSource<EventT>() {
      @Override
      public Registration addHandler(final EventHandler<? super EventT> handler) {
        final List<Registration> itemRegs = new ArrayList<>();
        for (ItemT item : list) {
          itemRegs.add(selector.select(item).addHandler(handler));
        }


        final Registration listReg = list.addListener(new CollectionAdapter<ItemT>() {
          @Override
          public void onItemAdded(CollectionItemEvent<? extends ItemT> event) {
            itemRegs.add(event.getIndex(), selector.select(event.getNewItem()).addHandler(handler));
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
}