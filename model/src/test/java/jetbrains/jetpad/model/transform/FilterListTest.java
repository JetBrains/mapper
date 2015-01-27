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
package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FilterListTest {
  private ObservableArrayList<String> from = new ObservableArrayList<>();
  private ObservableArrayList<String> to = new ObservableArrayList<>();
  Transformer<ObservableCollection<String>,ObservableList<String>> filter = Transformers.listFilter(new Function<String, ReadableProperty<Boolean>>() {
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
    assertTrue(to.get(0).equals("aa"));
  }

  @Test
  public void addToFromAfterNull() {
    from.add("null");
    filter.transform(from, to);
    from.add("aa");
    assertTrue(to.size() == 1);
    assertTrue(to.get(0).equals("aa"));
  }

  @Test
  public void simultaneousAdd() {
    final Property<Boolean> p = new ValueProperty<>(false);
    Transformer<ObservableCollection<Object>, ObservableList<Object>> filter = Transformers.listFilter(new Function<Object, ReadableProperty<Boolean>>() {
      @Override
      public ReadableProperty<Boolean> apply(Object o) {
        return p;
      }
    });
    ObservableList<Object> source = new ObservableArrayList<>();
    ObservableList<Object> target = filter.transform(source).getTarget();
    source.add("d");
    source.add(0, "c");
    source.add(0, "b");
    source.add(0, "a");
    p.set(true);

    assertEquals(4, target.size());
    assertEquals("a", target.get(0));
    assertEquals("b", target.get(1));
    assertEquals("c", target.get(2));
    assertEquals("d", target.get(3));
  }

  @Test
  public void simultaneousAddRemove() {
    final Property<Boolean> p = new ValueProperty<>(false);
    Transformer<ObservableCollection<Integer>, ObservableList<Integer>> filter = Transformers.listFilter(new Function<Integer, ReadableProperty<Boolean>>() {
      @Override
      public ReadableProperty<Boolean> apply(final Integer i) {
        return new DerivedProperty<Boolean>(p) {
          @Override
          public Boolean get() {
            return p.get() == (i % 2 == 0);
          }
        };
      }
    });
    ObservableList<Integer> source = new ObservableArrayList<>();
    ObservableList<Integer> target = filter.transform(source).getTarget();
    source.add(2);
    source.add(0, 1);
    p.set(true);

    assertEquals(1, target.size());
    assertEquals(2, (int)target.get(0));
  }

  @Test
  public void addSameElementTwice() {
    String s = "aa";
    from.add(s);
    from.add("bb");
    from.add(s);
    filter.transform(from, to);

    assertEquals(3, to.size());
    assertEquals("aa", to.get(0));
    assertEquals("bb", to.get(1));
    assertEquals("aa", to.get(2));
  }

  @Test
  public void addSameElementTwiceAfterTransformation() {
    filter.transform(from, to);

    String s = "aa";
    from.add(s);
    from.add("bb");
    from.add(s);

    assertEquals(3, to.size());
    assertEquals("aa", to.get(0));
    assertEquals("bb", to.get(1));
    assertEquals("aa", to.get(2));
  }

  @Test
  public void badPropertiesDependencies() {
    final Property<Boolean> init = new ValueProperty<>(false);

    ReadableProperty<Boolean> createTransformation = new DerivedProperty<Boolean>(init) {
      @Override
      public Boolean get() {
        return init.get();
      }
    };
    final ReadableProperty<Boolean> filter = new DerivedProperty<Boolean>(init) {
      @Override
      public Boolean get() {
        return init.get();
      }
    };

    createTransformation.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        Transformers.listFilter(new Function<String, ReadableProperty<Boolean>>() {
          @Nullable
          @Override
          public ReadableProperty<Boolean> apply(String s) {
            return filter;
          }
        }).transform(from, to);
      }
    });
    filter.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
      }
    });

    from.add("a");
    init.set(true);

    assertEquals(1, to.size());
    assertEquals("a", to.get(0));
  }
}