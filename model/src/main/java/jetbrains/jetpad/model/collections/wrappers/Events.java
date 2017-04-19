package jetbrains.jetpad.model.collections.wrappers;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.CollectionItemEvent;

class Events {
  static <TargetItemT, SourceItemT> CollectionItemEvent<TargetItemT> wrapEvent(
      CollectionItemEvent<? extends SourceItemT> event, Function<SourceItemT, TargetItemT> f) {
    TargetItemT oldItem = event.getOldItem() != null ? f.apply(event.getOldItem()) : null;
    TargetItemT newItem = event.getNewItem() != null ? f.apply(event.getNewItem()) : null;
    return new CollectionItemEvent<>(oldItem, newItem, event.getIndex(), event.getType());
  }
}
