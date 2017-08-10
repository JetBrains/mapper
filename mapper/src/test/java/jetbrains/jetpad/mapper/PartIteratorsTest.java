/*
 * Copyright 2012-2017 JetBrains s.r.o
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

import com.google.common.collect.Iterables;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class PartIteratorsTest extends BaseTestCase {
  @Test
  public void synchronizersIterator() {
    Mapper<Void, Void> mapper = new Mapper<Void, Void>(null, null) {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(Synchronizers.forRegistration(Registration.EMPTY));
      }
    };
    assertEquals(0, Iterables.size(mapper.synchronizers()));

    mapper.attachRoot();
    assertEquals(1, Iterables.size(mapper.synchronizers()));

    ObservableSet<Mapper<?, ?>> children = mapper.createChildSet();
    children.add(new Mapper<Void, Void>(null, null) {});
    assertEquals(1, Iterables.size(mapper.synchronizers()));
  }

  @Test
  public void childrenIterator() {
    Mapper<Object, Object> mapper = new Mapper<Object, Object>(new Object(), new Object()) {
      @Override
      protected void registerSynchronizers(Mapper.SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(Synchronizers.forRegistration(Registration.EMPTY));
      }
    };

    assertEquals(0, Iterables.size(mapper.children()));

    mapper.attachRoot();
    assertEquals(0, Iterables.size(mapper.children()));
    assertEquals(1, Iterables.size(mapper.synchronizers()));

    mapper.createChildList().add(new Mapper<Object, Object>(new Object(), new Object()) {});
    Mapper<Object, Object> setChild = new Mapper<Object, Object>(new Object(), new Object()) {};
    ObservableSet<Mapper<?, ?>> childSet = mapper.createChildSet();
    childSet.add(setChild);
    Property<Mapper<?, ?>> childProperty = mapper.createChildProperty();
    childProperty.set(new Mapper<Object, Object>(new Object(), new Object()) {});
    assertEquals(3, Iterables.size(mapper.children()));

    childProperty.set(null);
    assertEquals(2, Iterables.size(mapper.children()));

    Iterator<Mapper<?, ?>> i = childSet.iterator();
    i.next();
    i.remove();

    assertEquals(1, Iterables.size(mapper.children()));
  }
}