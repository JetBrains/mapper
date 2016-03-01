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
package jetbrains.jetpad.mapper;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class MappingContextTest extends BaseTestCase {
  private static final MappingContextProperty<String> TEST = new MappingContextProperty<>("test");

  private MappingContext context = new MappingContext();

  @Test
  public void registerNonFindableMapper() {
    Mapper mapper = createNonFindableMapper();

    final Value<Boolean> mapperRegistered = new Value<>(false);
    context.addListener(new MappingContextListener() {
      @Override
      public void onMapperRegistered(Mapper<?, ?> mapper) {
        mapperRegistered.set(true);
      }

      @Override
      public void onMapperUnregistered(Mapper<?, ?> mapper) {
      }
    });

    mapper.attach(context);

    assertTrue(mapperRegistered.get());
  }

  @Test
  public void unregisterNonFindableMapper() {
    Mapper mapper = createNonFindableMapper();
    mapper.attach(context);

    final Value<Boolean> mapperUnregistered = new Value<>(false);
    context.addListener(new MappingContextListener() {
      @Override
      public void onMapperRegistered(Mapper<?, ?> mapper) {
      }

      @Override
      public void onMapperUnregistered(Mapper<?, ?> mapper) {
        mapperUnregistered.set(true);
      }
    });

    mapper.detach();

    assertTrue(mapperUnregistered.get());
  }

  @Test
  public void unknownProperty() {
    assertNull(context.get(TEST));
  }

  @Test
  public void putGet() {
    context.put(TEST, "value");
    assertEquals("value", context.get(TEST));
  }

  @Test(expected = IllegalStateException.class)
  public void putAllowedOnce() {
    context.put(TEST, "value");
    context.put(TEST, "another value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullNotAllowed() {
    context.put(TEST, null);
  }

  private ItemMapper createNonFindableMapper() {
    return new ItemMapper(new Item()) {
      @Override
      protected boolean isFindable() {
        return false;
      }
    };
  }
}
