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
package jetbrains.jetpad.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapperTest {
  private Item source = createItemTree();
  private ItemMapper mapper;
  private Item target;

  public MapperTest() {
  }

  @Before
  public void init() {
    mapper = new ItemMapper(source);
    mapper.attachRoot();
    target = mapper.getTarget();
  }

  @Test
  public void initialMapping() {
    assertMapped();
  }

  @Test
  public void propertyChange() {
    source.name.set("xyz");

    assertMapped();
  }

  @Test
  public void removeItemFromObservable() {
    source.observableChildren.remove(0);

    assertMapped();
  }

  @Test
  public void addItemToObservable() {
    source.observableChildren.add(0, new Item());

    assertMapped();
  }

  @Test
  public void removeItemFromSimple() {
    source.children.remove(0);
    mapper.refreshSimpleRole();

    assertMapped();
  }

  @Test
  public void addItemToSimple() {
    source.children.add(new Item());
    mapper.refreshSimpleRole();

    assertMapped();
  }

  @Test
  public void singleChildSet() {
    source.singleChild.set(new Item());

    assertMapped();
  }

  @Test
  public void singleChildSetToNull() {
    source.singleChild.set(new Item());
    source.singleChild.set(null);

    assertMapped();
  }

  @Test
  public void rolesAreCleanedOnDetach() {
    assertFalse(target.observableChildren.isEmpty());
    assertFalse(target.children.isEmpty());
    assertFalse(target.transformedChildren.isEmpty());

    mapper.detachRoot();

    assertTrue(target.observableChildren.isEmpty());
    assertTrue(target.children.isEmpty());
    assertTrue(target.transformedChildren.isEmpty());
  }

  private void assertMapped() {
    Assert.assertEquals(source, target);
  }

  private static Item createItemTree() {
    Item result = new Item();
    result.name.set("xyz");

    for (int i = 0; i < 3; i++) {
      Item child = new Item();
      child.name.set("child" + i);
      result.observableChildren.add(child);
      result.children.add(child);
      result.transformedChildren.add(child);
    }

    return result;
  }
}