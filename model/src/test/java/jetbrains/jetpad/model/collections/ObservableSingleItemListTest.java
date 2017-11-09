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
package jetbrains.jetpad.model.collections;

import jetbrains.jetpad.model.collections.list.ObservableSingleItemList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ObservableSingleItemListTest {
  private ObservableSingleItemList<Integer> list = new ObservableSingleItemList<>();

  @Test(expected = IndexOutOfBoundsException.class)
  public void getItemOfEmpty() {
    assertTrue(list.isEmpty());
    list.getItem();
  }

  @Test
  public void addValue() {
    list.add(1);
    assertEquals(1, list.size());
    assertTrue(list.contains(1));
  }

  @Test
  public void setNullValue() {
    list.add(0);
    list.set(0, null);
    assertEquals(1, list.size());
    assertNull(list.getItem());
  }

  @Test
  public void setItemToEmptyList() {
    assertTrue(list.isEmpty());
    list.setItem(0);
    assertFalse(list.isEmpty());
  }

  @Test
  public void simpleSetItem() {
    list.add(0);
    list.setItem(1);
    assertEquals(1, (int) list.getItem());
  }

  @Test
  public void clearEmptyList() {
    assertTrue(list.isEmpty());
    list.clear();
    assertTrue(list.isEmpty());
  }
}