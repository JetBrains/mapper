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
package jetbrains.jetpad.model.id;

import jetbrains.jetpad.test.BaseTestCase;
import jetbrains.jetpad.test.Slow;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseIdTest extends BaseTestCase {

  @Test
  public void idEquality() {
    MyId id1 = new MyId("abc.def");
    MyId id2 = new MyId("abc.defz");
    MyId id3 = new MyId("abc.def");

    assertEquals(id1, id3);
    assertFalse(id2.equals(id3));
  }

  @Test
  public void sameName() {
    new MyId("a.b", "myName");
    new MyId("a.b", "myName");
  }

  @Test(expected = IllegalStateException.class)
  public void duplicateName() {
    new MyId("c.d", "newName1");
    new MyId("c.d", "newName2");
  }

  @Test
  public void nameDiscovery () {
    new MyId("e.f", "newName1");
    MyId id2 = new MyId("e.f");

    assertEquals("newName1", id2.getName());
  }

  @Test
  public void stringRepresentation() {
    assertEquals("newName1 [g.h]", new MyId("g.h", "newName1").toString());
    assertEquals("i.j", new MyId("i.j").toString());
  }

  @Slow
  @Test
  public void idRandomness() {
    int[] stats = new int[128];

    for (int i = 0; i < 62000; i++) {
      String id = new BaseId() {}.getId();
      for (int j = 0; j < id.length(); j++) {
        stats[id.charAt(j)] ++;
      }
    }

    checkUniform(stats, 'A', 'Z', 22000);
    checkUniform(stats, 'a', 'z', 22000);
    checkUniform(stats, '0', '9', 22000);
  }

  private void checkUniform(int[] stats, char a, char z, int expected) {
    for (int i = a; i <= z; i++) {
      assertTrue(Math.abs(stats[i] - expected) < expected / 10);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyId() {
    new MyId("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullId() {
    new MyId(null);
  }

  private static class MyId extends BaseId {
    private MyId(String id) {
      super(id);
    }

    private MyId(String id, String name) {
      super(id, name);
    }
  }
}