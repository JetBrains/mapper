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

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectsTest extends BaseTestCase {
  @Test
  public void bothNull() {
    assertTrue(Objects.equal(null, null));
  }

  @Test
  public void firstNull() {
    assertFalse(Objects.equal(null, new Object()));
  }

  @Test
  public void secondNull() {
    assertFalse(Objects.equal(new Object(), null));
  }

  @Test
  public void objects() {
    assertFalse(Objects.equal(new Object(), new Object()));
    assertTrue(Objects.equal(new Integer("1"), new Integer("1")));
  }

  @Test
  public void strings() {
    assertFalse(Objects.equal("a", "aa"));
    assertFalse(Objects.equal("aa", "a"));
    assertFalse(Objects.equal("aa", "ab"));
    assertTrue(Objects.equal("aa", "aa"));
    assertTrue(Objects.equal("", ""));
  }
}