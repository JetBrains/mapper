/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ByTargetIndexTest extends BaseTestCase {
  private Map<Item, Item> sourceToTarget = new HashMap<>();

  private Item item;
  private Item hidden_item;
  private Item child;
  private Item hidden_child;
  private ByTargetIndex finder;

  @Before
  public void init() {
    item = new Item();
    child = new Item();
    hidden_child = new Item();
    item.observableChildren.add(child);

    MyItemMapper mapper = new MyItemMapper(item);
    mapper.attachRoot();

    hidden_item = new Item();
    hidden_item.observableChildren.add(hidden_child);
    mapper.createChildProperty().set(new MyNotFinableItemMapper(hidden_item));

    finder = new ByTargetIndex(mapper.getMappingContext());
  }

  @Test
  public void findMapper() {
    assertFound(child);
    assertNotFound(hidden_child);
  }

  @Test
  public void removeMapper() {
    item.observableChildren.remove(child);

    assertNotFound(child);

    // no exception is thrown
    hidden_item.observableChildren.remove(hidden_child);
  }

  @Test
  public void addMapper() {
    Item anotherChild = new Item();
    child.observableChildren.add(anotherChild);

    assertFound(anotherChild);

    anotherChild = new Item();
    hidden_child.observableChildren.add(anotherChild);
    assertNotFound(anotherChild);
  }

  @Test
  public void addThenRemoveMapper() {
    Item anotherChild = new Item();
    child.observableChildren.add(anotherChild);
    child.observableChildren.remove(anotherChild);

    assertNotFound(anotherChild);
  }

  @Test
  public void addWhenFinderDisposed() {
    finder.dispose();

    Item anotherChild = new Item();
    child.observableChildren.add(anotherChild);

    assertNotFound(anotherChild);
  }

  private void assertFound(Item child) {
    assertEquals(1, finder.getMappers(sourceToTarget.get(child)).size());
  }

  private void assertNotFound(Item child) {
    assertEquals(0, finder.getMappers(sourceToTarget.get(child)).size());
  }

  private class MyItemMapper extends ItemMapper {
    MyItemMapper(Item item) {
      super(item);
      sourceToTarget.put(item, getTarget());
    }

    @Override
    protected MapperFactory<Item, Item> createMapperFactory() {
      return new MapperFactory<Item, Item>() {
        @Override
        public Mapper<? extends Item, ? extends Item> createMapper(Item source) {
          return new MyItemMapper(source);
        }
      };
    }
  }

  private class MyNotFinableItemMapper extends MyItemMapper {
    MyNotFinableItemMapper(Item item) {
      super(item);
    }

    @Override
    protected boolean isFindable() {
      return false;
    }

    protected MapperFactory<Item, Item> createMapperFactory() {
      return new MapperFactory<Item, Item>() {
        @Override
        public Mapper<? extends Item, ? extends Item> createMapper(Item source) {
          return new ItemMapper(source) {
            @Override
            protected boolean isFindable() {
              return false;
            }
          };
        }
      };
    }
  }
}
