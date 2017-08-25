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

import jetbrains.jetpad.base.function.Predicate;
import jetbrains.jetpad.base.function.Supplier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PropertyValidationTest {
  @Test
  public void validatedProperty() {
    Property<String> source = new ValueProperty<>("abc");

    Property<String> validated = Properties.validatedProperty(source,
        new Predicate<String>() {
          @Override
          public boolean test(String input) {
            if (input == null) return false;
            return input.length() > 3;
          }
        });

    assertNull(validated.get());

    source.set("aaaaa");
    assertEquals("aaaaa", validated.get());
  }

  @Test
  public void isValidProperty() {
    Property<String> source = new ValueProperty<>("abc");
    Supplier<Boolean> isValid = Properties.isPropertyValid(source,
        new Predicate<String>() {
          @Override
          public boolean test(String input) {
            if (input == null) return false;
            return input.length() > 1;
          }
        });
    assertTrue(isValid.get());

    source.set("z");
    assertFalse(isValid.get());
  }
}