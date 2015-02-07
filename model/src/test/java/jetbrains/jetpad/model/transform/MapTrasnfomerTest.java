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
package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MapTrasnfomerTest {
  private ObservableList<String> source = new ObservableArrayList<>();
  private ObservableList<Integer> target;
  private Transformer<ObservableList<String>, ObservableList<Integer>> transformer = Transformers.listMap(
    Transformers.fromFun(new Function<String, Integer>() {
      @Override
      public Integer apply(String input) {
        return input.length();
      }
    }));



  public void startTrans() {
    target = transformer.transform(source).getTarget();
  }

  @Test
  public void nonEmptySource() {
    source.add("aaa");

    startTrans();

    assertEquals(Arrays.asList(3), target);
  }

  @Test
  public void itemAdd() {
    startTrans();

    source.add("aaa");
    source.add(0, "b");

    assertEquals(Arrays.asList(1, 3), target);
  }


  @Test
  public void itemRemove() {
    startTrans();


    source.addAll(Arrays.asList("aaaa", "bbb", "c"));

    source.remove(1);

    assertEquals(Arrays.asList(4, 1), target);
  }

}