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
package jetbrains.jetpad.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PersistersTest {
  @Test
  public void nullInt() {
    testNull(Persisters.intPersister(10));
  }

  @Test
  public void nullLong() {
    testNull(Persisters.longPersister());
  }

  @Test
  public void nullBoolean() {
    testNull(Persisters.booleanPersister(true));
  }

  @Test
  public void nullDouble() {
    testNull(Persisters.doublePersister(1.5));
  }

  @Test
  public void nullString() {
    Persister<String> persister = Persisters.stringPersister();
    assertNull(persister.deserialize(persister.serialize(null)));
  }

  private <T> void testNull(Persister<T> persister) {
    T defaultValue = persister.deserialize(null);
    assertEquals(defaultValue, persister.deserialize(persister.serialize(null)));
  }
}
