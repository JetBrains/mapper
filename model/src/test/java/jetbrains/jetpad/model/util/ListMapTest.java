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
package jetbrains.jetpad.model.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListMapTest {
  private ListMap<String, String> list = new ListMap<>();
  private Map<String, String> map = new HashMap<>();
  private final static int RANDOM_STEPS = 100000;
  private Random random = new Random(1000);

  @Test
  public void empty() {
    assertEquals("{}", list.toString());
  }

  @Test
  public void put() {
    list.put("a", "b");
    assertEquals("{a=b}", list.toString());
  }

  @Test
  public void putNull() {
    list.put("a", null);
    assertEquals("{a=null}", list.toString());
  }

  @Test
  public void nullKey() {
    list.put(null, "b");
    assertEquals("{null=b}", list.toString());
  }

  @Test
  public void removeKey() {
    list.put("a", "b");
    list.remove("a");
    assertEquals("{}", list.toString());
  }
  
  @Test
  public void isEmpty() {
    assertTrue(list.isEmpty());
  }

  @Test
  public void isEmptyAfterRemove() {
    list.put("a", "b");
    list.remove("a");
    assertTrue(list.isEmpty());
  }

  @Test
  public void notEmpty() {
    list.put("a", "b");
    assertFalse(list.isEmpty());
  }

  @Test
  public void valueOfRemove() {
    list.put("a", "b");
    assertEquals("b", list.remove("a"));
  }

  @Test
  public void containsKey() {
    list.put("a", "b");
    assertTrue(list.containsKey("a"));
  }

  @Test
  public void notContainsKey() {
    list.put("a", "b");
    assertFalse(list.containsKey("b"));
  }

  @Test
  public void keySetRemove() {
    list.put("a", "b");
    list.put("c", "d");
    Iterator<String> it = list.keySet().iterator();
    it.next();
    it.next();
    it.remove();
    assertEquals("{a=b}", list.toString());
  }
  
  @Test
  public void random() {
    for (int i = 0; i < RANDOM_STEPS; i++) {
      doNextOp();
      assertEquals(map.size(), list.size());
    }
    assertEquals(map.keySet(), list.keySet());
    List<String> mapVals = new ArrayList<>(map.values());
    Collections.sort(mapVals);
    List<String> listVals = new ArrayList<>(list.values());
    Collections.sort(listVals);
    assertEquals(mapVals, listVals);

    assertEquals(map.keySet(), entriesToKeys(list.entrySet()));
    List<String> listValsFromEntries = entriesToValues(list.entrySet());
    Collections.sort(listValsFromEntries);
    assertEquals(mapVals, listValsFromEntries);
  } 
     
  private void doNextOp() {
    int r = random.nextInt(3);
    switch (r) {
      case 0: 
        doDelete();
        break;
      case 1: 
      case 2:
        doInsert();
        break;
      default:
        throw new IllegalStateException();
    }
  }
  
  private void doInsert() {
    String toInsertKey = nextString();
    String toInsertValue = nextString();
    assertEquals(map.put(toInsertKey, toInsertValue), list.put(toInsertKey, toInsertValue));
  }
  
  private void doDelete() {
    String toDelete = nextString();
    assertEquals(map.remove(toDelete), list.remove(toDelete));    
  }
  
  private String nextString() {    
    int index = random.nextInt(10);
    return Integer.toString(index);
  }

  private Set<String> entriesToKeys(Set<ListMap<String, String>.Entry> entries) {
    Set<String> keys = new HashSet<>();
    for (ListMap<String, String>.Entry entry : entries) {
      keys.add(entry.key());
    }
    return keys;
  }

  private List<String> entriesToValues(Set<ListMap<String, String>.Entry> entries) {
    List<String> values = new ArrayList<>(entries.size());
    for (ListMap<String, String>.Entry entry : entries) {
      values.add(entry.value());
    }
    return values;
  }
}