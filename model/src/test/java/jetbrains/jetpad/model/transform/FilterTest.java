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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.BaseReadableProperty;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilterTest {
  private ObservableCollection<String> from = new ObservableHashSet<>();
  private ObservableCollection<String> to = new ObservableHashSet<>();
  private Transformer<ObservableCollection<String>, ObservableCollection<String>> filter = Transformers.filter(new Function<String, ReadableProperty<Boolean>>() {
    @Override
    public ReadableProperty<Boolean> apply(String s) {
      Boolean value;
      if ("null".equals(s)) {
        value = null;
      } else {
        value = s.length() % 2 == 0;
      }
      return Properties.constant(value);
    }
  });

  @Test
  public void filterReturnsNull() {
    from.add("null");
    from.add("a");
    from.add("aa");
    filter.transform(from, to);
    assertEquals(1, to.size());
    assertEquals("aa", to.iterator().next());
  }

  @Test
  public void addToFromAfterNull() {
    from.add("null");
    filter.transform(from, to);
    from.add("aa");
    assertEquals(1, to.size());
    assertEquals("aa", to.iterator().next());
  }

  @Test
  public void lifecycle() {
    final Value<Integer> filterFunctionApplyCounter = new Value<>(0);
    final Value<Integer> filterPropertyGetCounter = new Value<>(0);

    final ReadableProperty<Boolean> p = new BaseReadableProperty<Boolean>() {
      @Override
      public Boolean get() {
        filterPropertyGetCounter.set(filterPropertyGetCounter.get() + 1);
        return true;
      }

      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Boolean>> handler) {
        return Registration.EMPTY;
      }
    };

    Transformers.filter(new Function<String, ReadableProperty<Boolean>>() {
      @Override
      public ReadableProperty<Boolean> apply(String s) {
        filterFunctionApplyCounter.set(filterFunctionApplyCounter.get() + 1);
        return p;
      }
    }).transform(from, to);

    String s = "abc";
    from.add(s);
    assertEquals(1, (int) filterFunctionApplyCounter.get());
    assertEquals(1, (int) filterPropertyGetCounter.get());
    assertTrue(to.contains(s));

    from.remove(s);
    assertTrue(from.isEmpty());
    assertTrue(to.isEmpty());
    assertEquals(1, (int) filterFunctionApplyCounter.get());
    assertEquals(1, (int) filterPropertyGetCounter.get());
  }

  @Test
  public void removeFilteredItem() {
    from.add("a");
    from.add("aa");
    filter.transform(from, to);

    assertEquals(1, to.size());
    assertFalse(to.contains("a"));
    from.remove("a");
    assertEquals(1, to.size());
  }
}