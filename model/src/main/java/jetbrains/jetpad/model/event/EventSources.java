package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Selector;

import java.util.ArrayList;
import java.util.List;

public class EventSources {
  @SafeVarargs
  public static <EventT> EventSource<EventT> composite(EventSource<? extends EventT>... sources) {
    return new CompositeEventSource<>(sources);
  }

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
