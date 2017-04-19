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

  public static <T> Matcher<CollectionItemEvent<? extends T>> addEvent(final Matcher<? super T> item, final Matcher<Integer> index) {
    return event(nullValue(), item, index, equalTo(ADD));
  }

  public static <T> Matcher<CollectionItemEvent<? extends T>> setEvent(final Matcher<? super T> oldItem, final Matcher<? super T> newItem, final Matcher<Integer> index) {
    return event(oldItem, newItem, index, equalTo(SET));
  }

  public static <T> Matcher<CollectionItemEvent<? extends T>> removeEvent(final Matcher<? super T> item, final Matcher<Integer> index) {
    return event(item, nullValue(), index, equalTo(REMOVE));
  }
}
