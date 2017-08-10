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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.TreeList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeListTest {
  @Test
  public void testTreeList() {
    Random random = new Random(239);
    int maxValue = 1000;

    for (int i = 0; i < 100; i++) {
      List<Integer> arrayList = new ArrayList<>();
      TreeList<Integer> treeList = new TreeList<>();

      for (int j = 0; j < 500; j++) {
        int op = random.nextInt(3);

        if (op == 0) {
          int index = random.nextInt(arrayList.size() + 1);
          int element = random.nextInt(maxValue);

          arrayList.add(index, element);
          treeList.add(index, element);
        } else if (op == 1 && !arrayList.isEmpty()) {
          int index = random.nextInt(arrayList.size());
          int element = random.nextInt(maxValue);

          arrayList.set(index, element);
          treeList.set(index, element);
        } else if (op == 2 && !arrayList.isEmpty()) {
          int index = random.nextInt(arrayList.size());

          arrayList.remove(index);
          treeList.remove(index);
        }

        treeList.check();
        if (!arrayList.equals(treeList)) {
          throw new IllegalStateException();
        }
      }
    }
  }
}