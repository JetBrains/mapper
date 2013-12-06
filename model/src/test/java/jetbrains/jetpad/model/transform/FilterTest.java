/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FilterTest {
  private ObservableCollection<String> from = new ObservableHashSet<String>();
  private ObservableCollection<String> to = new ObservableHashSet<String>();
  Transformer<ObservableCollection<String>, ObservableCollection<String>> filter = Transformers.filter(new Function<String, ReadableProperty<Boolean>>() {
    @Override
    public ReadableProperty<Boolean> apply(String s) {
      Boolean value;
      if (s.equals("null")) {
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
    assertTrue(to.size() == 1);
    assertTrue(to.iterator().next().equals("aa"));
  }

  @Test
  public void addToFromAfterNull() {
    from.add("null");
    filter.transform(from, to);
    from.add("aa");
    assertTrue(to.size() == 1);
    assertTrue(to.iterator().next().equals("aa"));
  }
}