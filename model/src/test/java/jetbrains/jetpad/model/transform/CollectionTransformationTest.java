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

import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.list.ObservableSingleItemList;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionTransformationTest {
  private static final Function<MyObject, ReadableProperty<Boolean>> IS_A = input -> Properties.equals(input.name, "a");

  private ObservableSet<MyObject> from = new ObservableHashSet<>();
  private ObservableSet<MyObject> to = new ObservableHashSet<>();
  private ObservableList<MyObject> toList = new ObservableArrayList<>();
  private ObservableList<MyObject> fromList = new ObservableArrayList<>();

  @Test
  public void filterInitial() {
    from.add(new MyObject("a"));
    from.add(new MyObject("b"));

    Transformers.filter(IS_A).transform(from, to);

    assertEquals("[a]", "" + to);
  }

  @Test
  public void filterHandling() {
    Transformers.filter(IS_A).transform(from, to);
    MyObject objectB = new MyObject("b");

    from.add(new MyObject("a"));
    from.add(objectB);
    from.remove(objectB);

    assertEquals("[a]", "" + to);
  }

  @Test
  public void listFilterInitial() {
    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("b"));

    Transformers.listFilter(IS_A).transform(fromList, toList);

    assertEquals("[a]", "" + toList);
  }

  @Test
  public void listFilterHandling() {
    Transformers.listFilter(IS_A).transform(fromList, toList);

    MyObject objectB = new MyObject("b");

    fromList.add(new MyObject("a"));
    fromList.add(objectB);
    fromList.remove(objectB);

    assertEquals("[a]", "" + toList);
  }

  @Test
  public void sortInitial() {
    from.add(new MyObject("a"));
    from.add(new MyObject("c"));
    from.add(new MyObject("b"));

    createSortTransformer().transform(from, toList);

    assertEquals("[a, b, c]", toList.toString());
  }

  @Test
  public void sortHandling() {
    createSortTransformer().transform(from, toList);

    from.add(new MyObject("a"));
    from.add(new MyObject("c"));
    from.add(new MyObject("b"));

    assertEquals("[a, b, c]", toList.toString());
  }

  @Test
  public void sortWithTwoTransformsRegistrationDisposeBug() {
    Transformer<ObservableCollection<MyObject>, ObservableList<MyObject>> trans = createSortTransformer();

    Transformation<ObservableCollection<MyObject>, ObservableList<MyObject>> transformation = trans.transform(new ObservableArrayList<MyObject>());

    trans.transform(from, toList);

    transformation.dispose();


    from.add(new MyObject("a"));

    assertEquals("[a]", toList.toString());
  }

  @Test
  public void sortWithTwoTransformsItemRegistrationDisposeBug() {
    Transformer<ObservableCollection<MyObject>, ObservableList<MyObject>> trans = createSortTransformer();

    Transformation<ObservableCollection<MyObject>, ObservableList<MyObject>> t1 = trans.transform(new ObservableArrayList<MyObject>());
    Transformation<ObservableCollection<MyObject>, ObservableList<MyObject>> t2 = trans.transform(new ObservableArrayList<MyObject>());


    MyObject o1 = new MyObject("a");
    t1.getSource().add(o1);
    t2.getSource().add(o1);

    t1.getSource().remove(o1);
    t2.getSource().remove(o1);
  }

  @Test
  public void addFirstHandling() {
    Transformers.<MyObject, MyObject, MyObject>addFirst(new MyObject("z")).transform(fromList, toList);

    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("c"));
    fromList.add(new MyObject("b"));

    assertEquals("[z, a, c, b]", toList.toString());
  }

  @Test
  public void flattenRemoveWithoutAddThrewNPE() {
    MyObject a = new MyObject("a");
    from.add(a);
    initForFlattenTest();
    from.remove(a);
    assertTrue(from.isEmpty());
  }

  @Test
  public void flattenImmediateDisposeThrewNPE() {
    MyObject a = new MyObject("a");
    from.add(a);
    initForFlattenTest().dispose();
    assertEquals("[a]", from.toString());
  }

  @Test
  public void flatten() {
    Transformation<ObservableCollection<MyObject>, ObservableCollection<MyObject>> transformation = initForFlattenTest();
    MyObject a = new MyObject("a");
    from.add(a);
    from.remove(a);
    transformation.dispose();
    assertTrue(from.isEmpty());
  }

  @Test
  public void flattenWithTransformer() {
    Function<MyObject, ObservableCollection<String>> f = input -> {
      ObservableHashSet<String> s = new ObservableHashSet<>();
      s.add(input.name.get() + "1");
      s.add(input.name.get() + "2");
      return s;
    };

    Transformer<ObservableCollection<String>, ObservableCollection<MyObject>> t = Transformers.oneToOne(
      input -> new MyObject(input),
      input -> input.name.get()
    );

    Transformer<ObservableCollection<MyObject>, ObservableCollection<MyObject>> flatten = Transformers.flatten(f, t);
    from.add(new MyObject("a"));

    flatten.transform(from, to);
    assertTrue(to.size() == 2);

    from.add(new MyObject("b"));
    assertTrue(to.size() == 4);
  }

  @Test
  public void addPlaceHolderIfEmpty() {
    Transformers.withPlaceHoldersIfEmpty(new Supplier<MyObject>() {
      @Override
      public MyObject get() {
        return new MyObject("p");
      }
    }).transform(fromList, toList);

    assertEquals("[p]", toList.toString());

    fromList.add(new MyObject("z"));
    assertEquals("[z]", toList.toString());

    fromList.clear();
    assertEquals("[p]", toList.toString());
  }

  @Test
  public void firstNInitial() {
    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("b"));
    fromList.add(new MyObject("c"));

    Transformers.<MyObject>firstN(Properties.constant(2)).transform(fromList, toList);

    assertEquals("[a, b]", toList.toString());
  }

  @Test
  public void firstNAdd() {
    Transformers.<MyObject>firstN(Properties.constant(1)).transform(fromList, toList);

    fromList.add(new MyObject("x"));
    fromList.add(0, new MyObject("y"));

    assertEquals("[y]", toList.toString());
  }

  @Test
  public void targetHasNoMoreThanNElements() {
    ObservableSingleItemList<MyObject> targetList = new ObservableSingleItemList<>();
    fromList.add(new MyObject("x"));

    Transformers.<MyObject>firstN(Properties.constant(1)).transform(fromList, targetList);
    fromList.add(0, new MyObject("y"));

    assertEquals("[y]", targetList.toString());
  }
  
  @Test
  public void firstNRemove() {
    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("b"));
    fromList.add(new MyObject("c"));

    Transformers.<MyObject>firstN(Properties.constant(1)).transform(fromList, toList);

    fromList.remove(2);
    fromList.remove(0);

    assertEquals("[b]", toList.toString());
  }

  @Test
  public void firstNincreaseN() {
    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("b"));
    fromList.add(new MyObject("c"));

    ValueProperty<Integer> prop = new ValueProperty<>(1);

    Transformers.<MyObject>firstN(prop).transform(fromList, toList);

    prop.set(2);

    assertEquals("[a, b]", toList.toString());
  }

  @Test
  public void firstNdecreaseN() {
    fromList.add(new MyObject("a"));
    fromList.add(new MyObject("b"));
    fromList.add(new MyObject("c"));

    ValueProperty<Integer> prop = new ValueProperty<>(2);

    Transformers.<MyObject>firstN(prop).transform(fromList, toList);

    prop.set(0);

    assertEquals("[]", toList.toString());
  }


  private Transformer<ObservableCollection<MyObject>, ObservableList<MyObject>> createSortTransformer() {
    return Transformers.sortBy(new Function<MyObject, ReadableProperty<String>>() {
      @Override
      public Property<String> apply(MyObject input) {
        return input.name;
      }
    });
  }

  private Transformation<ObservableCollection<MyObject>, ObservableCollection<MyObject>> initForFlattenTest() {
    to.add(new MyObject("b"));
    to.add(new MyObject("c"));
    Function<MyObject, ObservableCollection<MyObject>> selector = source -> to;
    return Transformers.flatten(selector).transform(from, to);
  }

  private static class MyObject {
    final Property<String> name = new ValueProperty<>(null);

    private MyObject(String name) {
      this.name.set(name);
    }

    @Override
    public String toString() {
      return name.get();
    }
  }
}
