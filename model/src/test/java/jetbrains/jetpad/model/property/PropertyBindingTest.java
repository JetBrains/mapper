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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.event.Registration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PropertyBindingTest {
  private Property<String> source = new ValueProperty<>();
  private Property<String> target = new ValueProperty<>();

  @Test
  public void bidirectionalSync() {
    source.set("239");
    Registration reg = PropertyBinding.bind(source, target);

    assertEquals("239", target.get());

    target.set("z");
    assertEquals("z", source.get());

    reg.remove();
    source.set("zzz");

    assertEquals("z", target.get());
  }
}