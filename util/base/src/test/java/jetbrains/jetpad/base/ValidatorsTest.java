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
package jetbrains.jetpad.base;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidatorsTest {
  @Test
  public void signedInteger() {
    assertTrue(Validators.integer().test("239"));
    assertTrue(Validators.integer().test("-239"));
    assertFalse(Validators.integer().test(null));
  }

  @Test
  public void unsignedInteger() {
    assertTrue(Validators.unsignedInteger().test("239"));
    assertFalse(Validators.unsignedInteger().test("-239"));
    assertFalse(Validators.unsignedInteger().test("+239"));
    assertFalse(Validators.unsignedInteger().test(null));
  }
}