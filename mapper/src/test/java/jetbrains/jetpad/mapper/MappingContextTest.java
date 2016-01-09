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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MappingContextTest extends BaseTestCase {
  MappingContext context = new MappingContext();

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
  public void selectMapper() {
    final Mapper m1 = new ItemMapper(new Item());
    final Mapper m2 = new ItemMapper(new Item());

    for (Mapper<?, ?> m : new Mapper[] { m1, m2 }) {
      m.attach(context);
    }

    Set<Mapper<?, ?>> selection = context.selectMappers(new Predicate<Mapper<?, ?>>() {
      @Override
      public boolean apply(Mapper<?, ?> mapper) {
        return mapper == m1;
      }
    });

    assertEquals(selection, Sets.newHashSet(m1));
  }

  @Test
  public void selectMappers() {
    final Item i1 = new Item();
    Mapper m1 = new ItemMapper(i1);
    Mapper n1 = new ItemMapper(i1);
    Mapper m2 = new ItemMapper(new Item());

    for (Mapper<?, ?> m : new Mapper[] { m1, n1, m2 }) {
      m.attach(context);
    }

    Set<Mapper<?, ?>> selection = context.selectMappers(new Predicate<Mapper<?, ?>>() {
      @Override
      public boolean apply(Mapper<?, ?> mapper) {
        return mapper.getSource() == i1;
      }
    });
    assertEquals(selection, Sets.newHashSet(m1, n1));
  }

  @Test
  public void notFound() {
    Mapper m = new ItemMapper(new Item());
    m.attach(context);
    Set<Mapper<?, ?>> selection = context.selectMappers(Predicates.<Mapper<?, ?>>alwaysFalse());
    assertTrue(selection.isEmpty());
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
