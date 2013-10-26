package jetbrains.jetpad.model.children;

import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class CompositesTest {
  private TestComposite root = new TestComposite();
  private TestComposite child1 = new TestComposite();
  private TestComposite leaf11 = new TestComposite();
  private TestComposite leaf12 = new TestComposite();
  private TestComposite child2 = new TestComposite();
  private TestComposite leaf21 = new TestComposite();
  private TestComposite leaf22 = new TestComposite();

  @Before
  public void init() {
    root.children().addAll(Arrays.asList(child1, child2));
    child1.children().addAll(Arrays.asList(leaf11, leaf12));
    child2.children().addAll(Arrays.asList(leaf21, leaf22));
  }

  @Test
  public void firstLeaf() {
    assertSame(leaf11, Composites.firstLeaf(root));
  }

  @Test
  public void lastLeaf() {
    assertSame(leaf22, Composites.lastLeaf(root));
  }

  @Test
  public void prevSibling() {
    assertNull(Composites.prevSibling(leaf11));
    assertSame(leaf11, Composites.prevSibling(leaf12));
  }

  @Test
  public void nextSibling() {
    assertNull(Composites.nextSibling(leaf12));
    assertSame(leaf12, Composites.nextSibling(leaf11));
  }

  @Test
  public void nextLeafSibling() {
    assertSame(leaf12, Composites.nextLeaf(leaf11));
  }

  @Test
  public void nextLeafNonSibling() {
    assertSame(leaf21, Composites.nextLeaf(leaf12));
  }

  @Test
  public void nextLeafNone() {
    assertNull(Composites.nextLeaf(leaf22));
  }

  @Test
  public void prevLeafSibling() {
    assertSame(leaf11, Composites.prevLeaf(leaf12));
  }

  @Test
  public void prevLeafNonSibling() {
    assertSame(leaf12, Composites.prevLeaf(leaf21));
  }

  @Test
  public void prevLeafNone() {
    assertNull(Composites.prevLeaf(leaf11));
  }

  @Test
  public void nextLeaves() {
    assertEquals(Arrays.asList(leaf12, leaf21, leaf22), asList(Composites.nextLeaves(leaf11)));
  }

  @Test
  public void prevLeaves() {
    assertEquals(Arrays.asList(leaf12, leaf11), asList(Composites.prevLeaves(leaf21)));
  }

  @Test
  public void simpleAncestors() {
    assertEquals(Arrays.asList(child1, root), Composites.toList(Composites.ancestors(leaf11)));
  }

  @Test
  public void sameParentIsBefore() {
    assertTrue(Composites.isBefore(leaf11, leaf12));
  }

  @Test
  public void differentParentsIsBefore() {
    assertTrue(Composites.isBefore(leaf11, leaf22));
  }

  @Test
  public void itemsNotBeforeItself() {
    assertFalse(Composites.isBefore(leaf11, leaf11));
  }

  @Test(expected = IllegalArgumentException.class)
  public void oneAncestorOfOtherInIsBefore() {
    Composites.isBefore(root, leaf11);
  }

  @Test(expected = IllegalArgumentException.class)
  public void differentTreesInIsBefore() {
    Composites.isBefore(root, new TestComposite());
  }

  private List<TestComposite> asList(Iterable<TestComposite> it) {
    List<TestComposite> result = new ArrayList<TestComposite>();
    for (TestComposite v : it) {
      result.add(v);
    }
    return result;
  }

  private class TestComposite extends HasParent<TestComposite, TestComposite> implements Composite<TestComposite> {
    private ObservableList<TestComposite> myChildren = new ChildList<TestComposite, TestComposite>(this);

    @Override
    public List<TestComposite> children() {
      return myChildren;
    }
  }
}
