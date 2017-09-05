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
package jetbrains.jetpad.model.children;

import jetbrains.jetpad.model.property.ValueProperty;

public class ChildProperty<ParentT, ChildT extends SimpleComposite<? super ParentT, ? super ChildT>>
    extends ValueProperty<ChildT> {
  private ParentT myParent;

  public ChildProperty(ParentT parent) {
    myParent = parent;
  }

  @Override
  public void set(ChildT value) {
    if (get() == value) return;

    if (value != null && value.parent().get() != null) {
      throw new IllegalStateException();
    }

    ChildT oldValue = get();
    if (oldValue != null) {
      oldValue.myParent.set(null);
      oldValue.myPositionData = null;
    }
    if (value != null) {
      value.myParent.set(myParent);
      value.myPositionData = new PositionData<ChildT>() {
        @Override
        public Position<ChildT> get() {
          return new Position<ChildT>() {
            @Override
            public ChildT get() {
              return ChildProperty.this.get();
            }

            @Override
            public Object getRole() {
              return ChildProperty.this;
            }
          };
        }

        @Override
        public void remove() {
          set(null);
        }
      };
    }

    super.set(value);

    if (oldValue != null) {
      oldValue.myParent.flush();
    }
    if (value != null) {
      value.myParent.flush();
    }
  }
}