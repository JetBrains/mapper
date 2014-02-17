/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;
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
    TreePath<TestComposite> path1 = new TreePath<TestComposite>(child1);

    assertSame(child1, path1.get(root));
  }

  @Test
  public void pathEquality() {
    assertThat(new TreePath<TestComposite>(child1), equalTo(new TreePath<TestComposite>(child1)));
    assertThat(new TreePath<TestComposite>(child2), not(equalTo(new TreePath<TestComposite>(child1))));
  }

  @Test
  public void pathComparison() {
    assertTrue(new TreePath<TestComposite>(root).compareTo(new TreePath<TestComposite>(child1)) < 0);
    assertTrue(new TreePath<TestComposite>(child1).compareTo(new TreePath<TestComposite>(child2)) < 0);
    assertTrue(new TreePath<TestComposite>(child1).compareTo(new TreePath<TestComposite>(child1)) == 0);
  }
}