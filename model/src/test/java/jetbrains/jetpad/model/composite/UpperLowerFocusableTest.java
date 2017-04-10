package jetbrains.jetpad.model.composite;

import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.model.composite.TestComposite.create;
import static org.junit.Assert.assertSame;

public class UpperLowerFocusableTest {

  @Test
  public void upperFocusable() {
    TestComposite root = create(0, 0, 10, 2);

    TestComposite row1 = create(0, 0, 10, 1);
    TestComposite row2 = create(0, 1, 10, 1);

    TestComposite left = create(1, 0, 3, 1);
    TestComposite right = create(5, 0, 3, 1);

    row1.children().addAll(Arrays.asList(left, right));

    root.children().addAll(Arrays.asList(row1, row2));

    assertPartitionsUp(row2, left, right, 4);
  }

  @Test
  public void lowerFocusable() {
    TestComposite root = create(0, 0, 10, 2);

    TestComposite row1 = create(0, 0, 10, 1);
    TestComposite row2 = create(0, 1, 10, 1);

    TestComposite left = create(1, 1, 5, 1);
    TestComposite right = create(8, 1, 1, 1);

    row2.children().addAll(Arrays.asList(left, right));

    root.children().addAll(Arrays.asList(row1, row2));

    assertPartitionsDown(row1, left, right, 7);
  }

  private void assertPartitionsUp(TestComposite bottom, TestComposite left, TestComposite right, int border) {
    assertPartitions(bottom, left, right, border, true);
  }

  private void assertPartitionsDown(TestComposite upper, TestComposite left, TestComposite right, int border) {
    assertPartitions(upper, left, right, border, false);
  }

  private void assertPartitions(TestComposite from, TestComposite left, TestComposite right, int border, boolean up) {
    for (int i = 0; i < 10; i++) {
      boolean chooseLeft = i <= border;
      TestComposite expected = chooseLeft ? left : right;
      assertSame((chooseLeft ? "Left" : "Right") + " expected.", expected, up ?
          Composites.upperFocusable(from, i):
          Composites.lowerFocusable(from, i));
    }
  }
}
