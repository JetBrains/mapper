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
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class FlattenListTest {
  private ObservableArrayList<String> from = new ObservableArrayList<>();
  private ObservableArrayList<String> to = new ObservableArrayList<>();
  private Transformer<ObservableList<String>, ObservableList<String>> transformer = Transformers.flattenList(new Function<String, ObservableList<String>>() {
    @Override
    public ObservableList<String> apply(String input) {
      ObservableList<String> res = new ObservableArrayList<>();
      res.add(input + "1");
      res.add(input + "2");
      return res;
    }
  });

  private ObservableArrayList<MyList> fromList = new ObservableArrayList<>();
  private Transformer<ObservableList<MyList>, ObservableList<String>> listTransformer = Transformers.flattenList(
    new Function<MyList, ObservableList<String>>() {
    @Override
    public ObservableList<String> apply(MyList input) {
      return input.list;
    }
  });


  @Test
  public void before() {
    from.add("a");
    from.add("b");

    transformer.transform(from, to);

    assertEquals("[a1, a2, b1, b2]", "" + to);
  }

  @Test
  public void addFirst() {
    transformer.transform(from, to);

    from.add("a");

    assertEquals("[a1, a2]", "" + to);
  }

  @Test
  public void addInTheBeginning() {
    from.add("b");
    transformer.transform(from, to);

    from.add(0, "a");

    assertEquals("[a1, a2, b1, b2]", "" + to);
  }


  @Test
  public void addInTheMiddle() {
    from.add("a");
    from.add("b");
    transformer.transform(from, to);

    from.add(1, "c");

    assertEquals("[a1, a2, c1, c2, b1, b2]", "" + to);
  }

  @Test
  public void addInTheEnd() {
    from.add("a");
    transformer.transform(from, to);

    from.add("b");

    assertEquals("[a1, a2, b1, b2]", "" + to);
  }

  @Test
  public void remove() {
    from.add("a");
    from.add("b");
    transformer.transform(from, to);

    from.remove(1);

    assertEquals("[a1, a2]", "" + to);
  }

  @Test
  public void removeAndAddAgain() {
    String a = "a";
    from.add(a);
    from.add("b");
    transformer.transform(from, to);

    from.remove(a);
    from.add(0, a);

    assertEquals("[a1, a2, b1, b2]", "" + to);
  }

  @Test
  public void addInList() {
    MyList l1 = new MyList();
    fromList.add(l1);
    listTransformer.transform(fromList, to);

    l1.list.add("a");

    MyList l2 = new MyList("b", "c");
    fromList.add(1, l2);

    assertEquals("[a, b, c]", "" + to);

    l2.list.add(1, "d");
    assertEquals("[a, b, d, c]", "" + to);
  }

  @Test
  public void removeInList() {
    MyList l1 = new MyList("a", "b");
    fromList.add(l1);
    listTransformer.transform(fromList, to);

    l1.list.remove(0);

    MyList l2 = new MyList("c", "d");
    fromList.add(1, l2);

    assertEquals("[b, c, d]", "" + to);
  }

  @Test
  public void addInCollectionAfterRemove() {
    MyList l1 = new MyList("a");
    fromList.add(l1);
    MyList l2 = new MyList("b");
    fromList.add(1, l2);
    listTransformer.transform(fromList, to);

    fromList.remove(l2);
    l2.list.add("c");

    assertEquals("[a]", "" + to);
  }

  @Test
  public void listOfListNPE() {
    ObservableList<String> l1 = new ObservableArrayList<>();
    ObservableList<String> l2 = new ObservableArrayList<>();
    ObservableList<ObservableList<? extends String>> container = new ObservableArrayList<>();
    container.add(l1);
    container.add(l2);

    ObservableList<String> result = Transformers.<String>flattenList().transform(container).getTarget();

    l1.add("x");

    assertEquals(Arrays.asList("x"), result);
  }

  private class MyList {
    ObservableArrayList<String> list = new ObservableArrayList<>();

    private MyList() {
    }

    private MyList(String... s) {
      list.addAll(Arrays.asList(s));
    }
  }
}