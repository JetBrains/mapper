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
package jetbrains.jetpad.model.collections;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.ADD;
import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.REMOVE;
import static jetbrains.jetpad.model.collections.CollectionItemEvent.EventType.SET;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

public class ObservableItemEventMatchers {
  public static <T> Matcher<CollectionItemEvent<? extends T>> event(final Matcher<? super T> oldItem, final Matcher<? super T> newItem,
                                                             final Matcher<Integer> index, final Matcher<CollectionItemEvent.EventType> type) {
    return new TypeSafeDiagnosingMatcher<CollectionItemEvent<? extends T>>() {
      @Override
      protected boolean matchesSafely(CollectionItemEvent<? extends T> event, Description description) {
        if (!type.matches(event.getType())) {
          description.appendText("type was ").appendValue(event.getType());
          return false;
        }
        if (!oldItem.matches(event.getOldItem())) {
          description.appendText("old item was ").appendValue(event.getOldItem());
          return false;
        }
        if (!newItem.matches(event.getNewItem())) {
          description.appendText("new item was ").appendValue(event.getNewItem());
          return false;
        }
        if (!index.matches(event.getIndex())) {
          description.appendText("index was ").appendValue(event.getIndex());
          return false;
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("an ").appendDescriptionOf(type).appendText(" event with ")
            .appendText("old item ").appendDescriptionOf(oldItem).appendText(", ")
            .appendText("new item ").appendDescriptionOf(newItem).appendText(", ")
            .appendText("index ").appendDescriptionOf(index);
      }
    };
  }

  public static <T> Matcher<CollectionItemEvent<? extends T>> addEvent(Matcher<? super T> item, Matcher<Integer> index) {
    return event(nullValue(), item, index, equalTo(ADD));
  }

  public static <T> Matcher<CollectionItemEvent<? extends T>> setEvent(Matcher<? super T> oldItem, Matcher<? super T> newItem, Matcher<Integer> index) {
    return event(oldItem, newItem, index, equalTo(SET));
  }

  public static <T> Matcher<CollectionItemEvent<? extends T>> removeEvent(Matcher<? super T> item, Matcher<Integer> index) {
    return event(item, nullValue(), index, equalTo(REMOVE));
  }
}