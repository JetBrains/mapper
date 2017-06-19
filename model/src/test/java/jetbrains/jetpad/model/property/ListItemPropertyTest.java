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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventMatchers.MatchingHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static jetbrains.jetpad.model.event.EventMatchers.allEvents;
import static jetbrains.jetpad.model.event.EventMatchers.newValue;
import static jetbrains.jetpad.model.event.EventMatchers.newValueIs;
import static jetbrains.jetpad.model.event.EventMatchers.noEvents;
import static jetbrains.jetpad.model.event.EventMatchers.oldValueIs;
import static jetbrains.jetpad.model.event.EventMatchers.setTestHandler;
import static jetbrains.jetpad.model.event.EventMatchers.singleEvent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class ListItemPropertyTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void rejectsNegativeIndex() {
    ObservableList<Integer> list = createList(5);
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, -1);
  }

  @Test
  public void rejectsTooSmallIndex() {
    ObservableList<Integer> list = new ObservableArrayList<>();
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, 0);
  }

  @Test
  public void rejectsTooLargeIndex() {
    ObservableList<Integer> list = createList(5);
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, 5);
  }

  @Test
  public void acceptsEdgeIndices() {
    ObservableList<Integer> list = createList(5);
    new ListItemProperty<>(list, 0);
    new ListItemProperty<>(list, 4);
  }

  @Test
  public void getsTheRightItem() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    assertEquals(2, p2.get().intValue());

    ListItemProperty<Integer> p4 = new ListItemProperty<>(list, 4);
    assertEquals(4, p4.get().intValue());
  }

  @Test
  public void setsTheRightItem() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    p2.set(12);
    assertEquals("[0, 1, 12, 3, 4]", "" + list);

    ListItemProperty<Integer> p4 = new ListItemProperty<>(list, 4);
    p4.set(14);
    assertEquals("[0, 1, 12, 3, 14]", "" + list);
  }

  @Test
  public void tracksItemOnAdd() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);
    list.add(2, 22);
    assertEquals(1, p1.get().intValue());
    assertEquals(2, p2.get().intValue());
    assertEquals(3, p3.get().intValue());

    p1.set(11);
    p2.set(12);
    p3.set(13);
    assertEquals("[0, 11, 22, 12, 13, 4]", "" + list);
  }

  @Test
  public void tracksItemOnRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);
    list.remove(2);
    assertEquals(1, p1.get().intValue());
    assertEquals(3, p3.get().intValue());
    assertFalse(p2.isValid());

    p1.set(11);
    p3.set(13);
    assertEquals("[0, 11, 13, 4]", "" + list);

    exception.expect(IllegalStateException.class);
    p2.set(12);
  }

  @Test
  public void firesOnListSet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    MatchingHandler<PropertyChangeEvent<Integer>> p1handler = setTestHandler(p1);
    MatchingHandler<PropertyChangeEvent<Integer>> p2handler = setTestHandler(p2);
    MatchingHandler<PropertyChangeEvent<Integer>> p3handler = setTestHandler(p3);

    list.add(2, 22);
    list.set(3, 12);

    assertThat(p1handler, noEvents());
    assertThat(p2handler, singleEvent(
        allOf(oldValueIs(2), newValueIs(12))));
    assertThat(p3handler, noEvents());
  }

  @Test
  public void firesOnTrackedItemRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 2);

    MatchingHandler<PropertyChangeEvent<Integer>> handler = setTestHandler(p);

    list.remove(2);

    assertThat(handler, singleEvent(
        allOf(oldValueIs(2), newValue(nullValue(Integer.class)))));
  }

  @Test
  public void firesOnPropertySet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);

    MatchingHandler<PropertyChangeEvent<Integer>> p2handler = setTestHandler(p2);

    p2.set(12);

    assertThat(p2handler, singleEvent(
        allOf(oldValueIs(2), newValueIs(12))));
  }

  @Test
  public void indexFiresOnListAdd() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);

    MatchingHandler<PropertyChangeEvent<Integer>> p1indexHandler = setTestHandler(p1.index);
    MatchingHandler<PropertyChangeEvent<Integer>> p2indexHandler = setTestHandler(p2.index);

    list.add(2, 22);

    assertThat(p1indexHandler, noEvents());
    assertThat(p2indexHandler, singleEvent(
        allOf(oldValueIs(2), newValueIs(3))));
  }

  @Test
  public void indexFiresOnListRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    MatchingHandler<PropertyChangeEvent<Integer>> p1indexHandler = setTestHandler(p1.index);
    MatchingHandler<PropertyChangeEvent<Integer>> p2indexHandler = setTestHandler(p2.index);
    MatchingHandler<PropertyChangeEvent<Integer>> p3indexHandler = setTestHandler(p3.index);

    list.remove(2);

    assertThat(p1indexHandler, noEvents());
    assertThat(p2indexHandler, singleEvent(
        allOf(oldValueIs(2), newValue(nullValue(Integer.class)))));
    assertThat(p3indexHandler, singleEvent(
        allOf(oldValueIs(3), newValueIs(2))));
  }

  @Test
  public void indexFiresNotOnListSet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    MatchingHandler<PropertyChangeEvent<Integer>> p1indexHandler = setTestHandler(p1.index);
    MatchingHandler<PropertyChangeEvent<Integer>> p2indexHandler = setTestHandler(p2.index);
    MatchingHandler<PropertyChangeEvent<Integer>> p3indexHandler = setTestHandler(p3.index);

    list.set(2, 22);

    assertThat(p1indexHandler, noEvents());
    assertThat(p2indexHandler, noEvents());
    assertThat(p3indexHandler, noEvents());
  }

  @Test
  public void disposeImmediately() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    p.dispose();
    exception.expect(IllegalStateException.class);
    p.dispose();
  }

  @Test
  public void disposeInvalid() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    list.remove(1);

    assertFalse(p.isValid());
    p.dispose();
    exception.expect(IllegalStateException.class);
    p.dispose();
  }

  @Test
  public void indexFiresNotOnDispose() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    MatchingHandler<PropertyChangeEvent<Integer>> indexHandler = setTestHandler(p.index);

    p.dispose();
    assertThat(indexHandler, noEvents());
  }

  @Test
  public void indexFiresNotOnDisposeInvalid() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    MatchingHandler<PropertyChangeEvent<Integer>> indexHandler = setTestHandler(p.index);

    list.remove(1);

    assertThat(indexHandler, allEvents(hasSize(1)));
    p.dispose();
    assertThat(indexHandler, allEvents(hasSize(1)));
  }


  private ObservableList<Integer> createList(int n) {
    ObservableList<Integer> list = new ObservableArrayList<>();
    for (int i = 0; i < n; i++) {
      list.add(i);
    }
    return list;
  }
}