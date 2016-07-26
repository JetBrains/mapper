/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

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
    TreePath<TestComposite> path1 = new TreePath<>(child1);

    assertSame(child1, path1.get(root));
  }

  @Test
  public void pathEquality() {
    assertThat(new TreePath<>(child1), equalTo(new TreePath<>(child1)));
    assertThat(new TreePath<>(child2), not(equalTo(new TreePath<>(child1))));
  }

  @Test
  public void pathComparison() {
    assertTrue(new TreePath<>(root).compareTo(new TreePath<>(child1)) < 0);
    assertTrue(new TreePath<>(child1).compareTo(new TreePath<>(child2)) < 0);
    assertTrue(new TreePath<>(child1).compareTo(new TreePath<>(child1)) == 0);
  }

  @Test
  public void sortByPath() {
    List<TestComposite> composites = new ArrayList<>(Arrays.asList(child2, child1, root));
    TreePath.sort(composites);
    assertEquals(Arrays.asList(root, child1, child2), composites);
  }

  @Test
  public void pathValidity() {
    TreePath<TestComposite> path = new TreePath<>(child1);

    assertTrue(path.isValid(root));
    assertFalse(path.isValid(child1));
  }

  @Test
  public void isEmpty() {
    TreePath<TestComposite> path = new TreePath<>(child1, child1);
    assertTrue(path.isEmpty());
  }

  @Test
  public void lastIndex() {
    TreePath<TestComposite> path = new TreePath<>(child2);
    assertEquals(1, path.getLastIndex());
  }

  @Test
  public void parent() {
    TreePath<TestComposite> path = new TreePath<>(child2);

    assertSame(root, path.getParent().get(root));
  }

  @Test
  public void restoreFromNonRoot() {
    TestComposite composite = new TestComposite();
    child1.children().add(composite);

    TreePath<TestComposite> path = new TreePath<>(composite, child1);
    assertSame(composite, path.get(child1));
  }

  @Test
  public void persistence() {
    TestComposite layer2 = new TestComposite();
    child1.children().add(layer2);

    TreePath<TestComposite> rootPath = new TreePath<>(root);
    assertEquals(rootPath, TreePath.<TestComposite>deserialize(rootPath.serialize()));

    TreePath<TestComposite> layer1path = new TreePath<>(child1, root);
    assertEquals(layer1path, TreePath.<TestComposite>deserialize(layer1path.serialize()));

    TreePath<TestComposite> layer2path = new TreePath<>(layer2, root);
    assertEquals(layer2path, TreePath.<TestComposite>deserialize(layer2path.serialize()));
  }
}
