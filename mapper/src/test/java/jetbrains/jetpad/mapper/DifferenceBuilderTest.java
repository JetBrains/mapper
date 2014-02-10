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
package jetbrains.jetpad.mapper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DifferenceBuilderTest {
  @Test
  public void listToEmpty() {
    assertConverges(Arrays.asList("a", "b", "c"), new ArrayList<String>());
  }

  @Test
  public void addOneItem() {
    assertConverges(Arrays.asList("a", "b", "c"), Arrays.asList("a", "c"));
  }

  @Test
  public void removeOneItem() {
    assertConverges(Arrays.asList("b", "c"), Arrays.asList("a", "b", "c"));
  }

  @Test
  public void rearrange() {
    assertConverges(Arrays.asList("a", "b", "c"), Arrays.asList("b", "c", "a"));
  }

  @Test
  public void addAndRearrange() {
    assertConverges(Arrays.asList("a", "b", "d", "e", "c"), Arrays.asList("b", "c", "a"));
  }

  private <ItemT> void assertConverges(List<ItemT> source, List<ItemT> target) {
    List<ItemT> targetList = new ArrayList<>(target);
    List<DifferenceBuilder<ItemT>.DifferenceItem> items = new DifferenceBuilder<>(source, targetList).build();
    for (DifferenceBuilder<ItemT>.DifferenceItem item : items) {
      item.apply(targetList);
    }
    assertThat(targetList, equalTo(source));
  }
}