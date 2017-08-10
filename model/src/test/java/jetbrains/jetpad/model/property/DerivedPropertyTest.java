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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DerivedPropertyTest {
  private Property<String> string = new ValueProperty<>("a");
  private ReadableProperty<Integer> length = new DerivedProperty<Integer>(string) {
    @Override
    protected Integer doGet() {
      return string.get().length();
    }
  };

  @Test
  public void propertyWithoutHandlers() {
    assertEquals(1, length.get().intValue());
    string.set("aa");
    assertEquals(2, length.get().intValue());
  }

  @Test
  public void handlerAddThenRemoved() {
    Registration reg = length.addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Integer> event) {
      }
    });
    reg.remove();

    string.set("aa");
    assertEquals(2, length.get().intValue());
  }

  @Test
  public void getBeforeEventReturnsOldValue() {
    final Value<Integer> lengthValue = new Value<>(0);
    final Value<Boolean> lengthEventFired = new Value<>(false);

    string.addHandler(new EventHandler<PropertyChangeEvent<String>>() {
      @Override
      public void onEvent(PropertyChangeEvent<String> event) {
        assertFalse(lengthEventFired.get());
        lengthValue.set(length.get());
      }
    });
    length.addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Integer> event) {
        lengthEventFired.set(true);
      }
    });

    string.set("aa");
    assertTrue(lengthEventFired.get());
    assertEquals(1, lengthValue.get().intValue());
    assertEquals(2, length.get().intValue());
  }
}