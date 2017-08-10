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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.base.Functions;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import jetbrains.jetpad.base.function.Function;

public class HighestPriorityErrorCasesTest {
  private static final Function<Object, Integer> INCONSISTENT_PRIORITY = new Function<Object, Integer>() {
    private int counter = 0;
    @Override
    public Integer apply(Object s) {
      return counter++;
    }
  };

  private static final Function<Object, Integer> NULL_PRIORITY = new Function<Object, Integer>() {
    @Override
    public Integer apply(Object s) {
      return null;
    }
  };

  private ObservableCollection<Object> from;

  @Before
  public void init() {
    from = new ObservableArrayList<>();
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullItem() {
    Transformers.highestPriority(Functions.constant(0)).transform(from);
    from.add(null);
  }

  @Test(expected = IllegalStateException.class)
  public void inconsistentPriority() {
    Transformers.highestPriority(INCONSISTENT_PRIORITY).transform(from);
    Object sample;
    from.addAll(Arrays.asList(sample = new Object(), new Object()));
    from.remove(sample);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullGetPriority() {
    Transformers.highestPriority(null).transform(from);
    from.add(new Object());
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullPriority() {
    Transformers.highestPriority(NULL_PRIORITY).transform(from);
    from.add(new Object());
  }

  @Test(expected = IllegalStateException.class)
  public void priorityGotNull() {
    final Value<Integer> priorityValue = new Value<>(0);
    Transformers.highestPriority(new Function<Object, Integer>() {
      @Override
      public Integer apply(Object o) {
        return priorityValue.get();
      }
    }).transform(from);
    Object sample;
    from.addAll(Arrays.asList(sample = new Object(), new Object()));
    priorityValue.set(null);
    from.remove(sample);
  }
}