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
package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class HighestPriorityIncorrectPriorityTest {
  private static final Function<String, Integer> INCONSISTENT_PRIORITY = new Function<String, Integer>() {
    private int counter = 0;
    @Override
    public Integer apply(String s) {
      return counter++;
    }
  };

  private ObservableCollection<String> from;

  @Before
  public void init() {
    from = new ObservableArrayList<>();
    from.addAll(Arrays.asList("a", "b"));
    Transformers.highestPriority(INCONSISTENT_PRIORITY).transform(from);
  }

  @Test(expected = IllegalStateException.class)
  public void remove() {
    from.remove("a");
  }
}
