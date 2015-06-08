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
package jetbrains.jetpad.model.children;

import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;

public class ChildList<ParentT, ChildT extends HasParent<? super ParentT, ? super ChildT>> extends ObservableArrayList<ChildT> {
  private ParentT myParent;

  public ChildList(ParentT parent) {
    myParent = parent;
    addListener(new CollectionAdapter<ChildT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ChildT> event) {
        event.getItem().myParent.flush();
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ChildT> event) {
        ChildT item = event.getItem();
        item.myParent.set(null);
        item.myPositionData = null;
        item.myParent.flush();
      }
    });
  }

  @Override
  protected void checkAdd(int index, ChildT item) {
    super.checkAdd(index, item);
    if (item.parent().get() != null) {
      throw new IllegalArgumentException();
    }
  }

  @Override
  protected void beforeItemAdded(int index, final ChildT item) {
    item.myParent.set(myParent);
    item.myPositionData = new PositionData<ChildT>() {
      @Override
      public Position<ChildT> get() {
        final int index = indexOf(item);
        return new Position<ChildT>() {
          @Override
          public ChildT get() {
            if (size() <= index) return null;
            return ChildList.this.get(index);
          }

          @Override
          public Object getRole() {
            return ChildList.this;
          }
        };
      }

      @Override
      public void remove() {
        ChildList.this.remove(item);
      }
    };
  }

  @Override
  protected void checkRemove(int index, ChildT item) {
    super.checkRemove(index, item);
    if (item.parent().get() != myParent) {
      throw new IllegalArgumentException();
    }
  }
}