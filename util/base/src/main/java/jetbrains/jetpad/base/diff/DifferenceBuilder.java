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
package jetbrains.jetpad.base.diff;

import java.util.ArrayList;
import java.util.List;

public final class DifferenceBuilder<ItemT> {
  private List<ItemT> mySourceList;
  private List<ItemT> myTargetList;

  public DifferenceBuilder(List<ItemT> sourceList, List<ItemT> targetList) {
    mySourceList = sourceList;
    myTargetList = targetList;
  }

  public List<DifferenceItem> build() {
    List<DifferenceItem> result = new ArrayList<>();

    List<ItemT> sourceContent = mySourceList;
    List<ItemT> target = new ArrayList<>(myTargetList);

    for (int i = target.size() - 1; i >= 0; i--) {
      ItemT current = target.get(i);
      if (!(sourceContent.contains(current))) {
        result.add(new DifferenceItem(i, current, false));
        target.remove(i);
      }
    }

    for (int i = 0; i < sourceContent.size(); i++) {
      ItemT current = sourceContent.get(i);
      ItemT next = null;
      if (i + 1 < sourceContent.size()) {
        next = sourceContent.get(i + 1);
      }

      if (target.size() <= i) {
        result.add(new DifferenceItem(i, current, true));
        target.add(i, current);
      } else {
        ItemT currentTarget = target.get(i);
        if (currentTarget != current) {
          int currentIndex = target.indexOf(current);
          if (currentIndex != -1) {
            result.add(new DifferenceItem(currentIndex, current, false));
            target.remove(current);
          }

          if (next == currentTarget) {
            result.add(new DifferenceItem(i, current, true));
            target.add(i, current);
          } else {
            result.add(new DifferenceItem(i, currentTarget, false));
            result.add(new DifferenceItem(i, current, true));
            target.set(i, current);
          }
        }
      }
     }

    return result;
  }

  public class DifferenceItem {
    public final int index;
    public final ItemT item;
    public final boolean isAdd;

    private DifferenceItem(int index, ItemT item, boolean add) {
      this.index = index;
      this.item = item;
      this.isAdd = add;
    }


    public void apply(List<ItemT> items) {
      if (isAdd) {
        items.add(index, item);
      } else {
        items.remove(index);
      }
    }

    @Override
    public String toString() {
      return (isAdd ? "add" : "remove") + " " + item + "@" + index;
    }
  }
}