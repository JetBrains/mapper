package jetbrains.jetpad.model.composite;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

public class TreePathTest {
  private TestComposite root = new TestComposite();
  private TestComposite child1 = new TestComposite();
  private TestComposite child2 = new TestComposite();

  @Before
  public void init() {
    root.children().addAll(Arrays.asList(child1, child2));
  }

  @Test
  public void saveRestore() {
    TreePath<TestComposite> path1 = new TreePath<TestComposite>(child1);

    assertSame(child1, path1.get(root));
  }

  @Test
  public void pathEquality() {
    assertEquals(new TreePath<TestComposite>(child1), new TreePath<TestComposite>(child1));
    assertNotEquals(new TreePath<TestComposite>(child2), new TreePath<TestComposite>(child1));
  }
}
