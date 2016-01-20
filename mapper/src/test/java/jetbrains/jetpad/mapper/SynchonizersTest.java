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

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SynchonizersTest extends BaseTestCase {
  @Test
  public void forEventSourceOnAttach() {
    final Value<Integer> runNum = new Value<>(0);
    Property<Boolean> prop = new ValueProperty<>();
    final Synchronizer synchronizer = Synchronizers.forEventSource(prop, new Runnable() {
      @Override
      public void run() {
        runNum.set(runNum.get() + 1);
      }
    });

    Mapper<Void, Void> mapper = new Mapper<Void, Void>(null, null) {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(synchronizer);
      }
    };
    mapper.attachRoot();

    assertEquals(1, (int)runNum.get());
  }

  @Test
  public void forEventSourceHandler() {
    final Property<Integer> prop = new ValueProperty<>();
    final List<Integer> handled = new ArrayList<>();

    Mapper<Void, Void> mapper = new Mapper<Void, Void>(null, null) {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(Synchronizers.forEventSource(
          prop,
          new Handler<PropertyChangeEvent<Integer>>() {
            @Override
            public void handle(PropertyChangeEvent<Integer> item) {
              handled.add(item.getNewValue());
            }
          }));
      }
    };

    mapper.attachRoot();
    assertTrue(handled.isEmpty());

    prop.set(1);
    prop.set(2);
    prop.set(3);
    assertEquals(Arrays.asList(1, 2, 3), handled);

    mapper.detachRoot();
    prop.set(4);
    assertEquals(Arrays.asList(1, 2, 3), handled);
  }
}
