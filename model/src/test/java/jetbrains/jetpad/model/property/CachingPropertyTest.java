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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CachingPropertyTest {
  Property<String> source = new ValueProperty<String>("b");
  ReadableProperty<String> cache = new CachingProperty<String>(source);

  @Test
  public void simpleMode() {
    source.set("a");
    assertEquals("a", cache.get());
  }

  @Test
  public void activeMode() {
    cache.addHandler(new EventHandler<PropertyChangeEvent<String>>() {
      @Override
      public void onEvent(PropertyChangeEvent<String> event) {
      }
    });

    source.set("a");
    assertEquals("a", cache.get());
  }
}
