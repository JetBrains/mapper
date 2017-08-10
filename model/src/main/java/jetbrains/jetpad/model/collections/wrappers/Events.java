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