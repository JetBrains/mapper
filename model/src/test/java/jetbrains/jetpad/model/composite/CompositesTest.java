/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composite;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
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
  public void removeFromParent() {
    Composites.removeFromParent(leaf11);
    assertEquals(Arrays.asList(leaf12), child1.children());
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

  @Test
  public void isFirstChildEverythingVisible() {
    assertTrue(Composites.isFirstChild(leaf11));
    assertFalse(Composites.isFirstChild(leaf12));
  }

  @Test
  public void isFirstChildFirstInvisible() {
    leaf11.visible().set(false);

    assertTrue(Composites.isFirstChild(leaf12));
  }

  @Test
  public void isLastChildEverythingVisible() {
    assertFalse(Composites.isLastChild(leaf11));
    assertTrue(Composites.isLastChild(leaf12));
  }

  @Test
  public void isLastChildLastInvisible() {
    assertTrue(Composites.isLastChild(leaf12));
  }

  @Test
  public void firstFocusableSimple() {
    assertSame(leaf11, Composites.firstFocusable(root));
  }

  @Test
  public void firstFocusableFirstLeafNonFocusable() {
    leaf11.focusable().set(false);

    assertSame(leaf12, Composites.firstFocusable(root));
  }

  @Test
  public void firstFocusableFirstLeafInvisible() {
    leaf11.visible().set(false);

    assertSame(leaf12, Composites.firstFocusable(root));
  }

  @Test
  public void lastFocusableSimple() {
    assertSame(leaf22, Composites.lastFocusable(root));
  }

  @Test
  public void lastFocusableLastLeafNonFocusable() {
    leaf22.focusable().set(false);

    assertSame(leaf21, Composites.lastFocusable(root));
  }

  @Test
  public void lastFocusableLastLeafInvisible() {
    leaf22.visible().set(false);

    assertSame(leaf21, Composites.lastFocusable(root));
  }

  @Test
  public void isVisibleInSimpleCase() {
    assertTrue(Composites.isVisible(leaf11));
  }

  @Test
  public void isVisibleItemInvisible() {
    leaf11.visible().set(false);
    assertFalse(Composites.isVisible(leaf11));
  }

  @Test
  public void isVisibleParentInvisible() {
    child1.visible().set(false);
    assertFalse(Composites.isVisible(leaf11));
  }

  @Test
  public void focusableParentSimpleCase() {
    assertSame(child1, Composites.focusableParent(leaf11));
  }

  @Test
  public void focusableParentParentNotFocusable() {
    child1.focusable().set(false);

    assertSame(root, Composites.focusableParent(leaf11));
  }

  @Test
  public void isFocusableSimpleCase() {
    assertTrue(Composites.isFocusable(leaf11));
  }

  @Test
  public void isFocusableParentInvisible() {
    child1.visible().set(false);

    assertFalse(Composites.isFocusable(leaf11));
  }

  @Test
  public void isDescendant() {
    assertTrue(Composites.isDescendant(root, leaf11));
    assertFalse(Composites.isDescendant(leaf11, root));
    assertFalse(Composites.isDescendant(leaf11, leaf12));
  }

  @Test
  public void nestFocusableSimple() {
    assertSame(leaf12, Composites.nextFocusable(leaf11));
  }

  @Test
  public void nestFocusableNoNext() {
    assertNull(Composites.nextFocusable(leaf22));
  }

  @Test
  public void nestFocusableNextUnfocusable() {
    leaf12.focusable().set(false);

    assertSame(leaf21, Composites.nextFocusable(leaf11));
  }

  @Test
  public void prevFocusableSimple() {
    assertSame(leaf11, Composites.prevFocusable(leaf12));
  }

  @Test
  public void prevFocusableNoPrev() {
    assertNull(Composites.prevFocusable(leaf11));
  }

  @Test
  public void prevFocusablePrevUnfocusable() {
    leaf12.focusable().set(false);

    assertSame(leaf11, Composites.prevFocusable(leaf21));
  }

  private List<TestComposite> asList(Iterable<TestComposite> it) {
    List<TestComposite> result = new ArrayList<TestComposite>();
    for (TestComposite v : it) {
      result.add(v);
    }
    return result;
  }

  private class TestComposite
      extends HasParent<TestComposite, TestComposite>
      implements Composite<TestComposite>, HasVisibility, HasFocusability {
    private ObservableList<TestComposite> myChildren = new ChildList<TestComposite, TestComposite>(this);
    private Property<Boolean> myVisible = new ValueProperty<Boolean>(true);
    private Property<Boolean> myFocusable = new ValueProperty<Boolean>(true);

    @Override
    public List<TestComposite> children() {
      return myChildren;
    }

    @Override
    public Property<Boolean> visible() {
      return myVisible;
    }

    @Override
    public Property<Boolean> focusable() {
      return myFocusable;
    }
  }
}