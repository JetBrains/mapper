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
