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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.model.property.PropertyChangeEvent;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.List;

public class EventMatchers {
  public static class MatchingHandler<EventT> implements EventHandler<EventT> {
    final List<EventT> events = new ArrayList<>();

    @Override
    public void onEvent(EventT event) {
      events.add(event);
    }
  }

  public static <EventT> MatchingHandler<EventT> setTestHandler(EventSource<? extends EventT> source) {
    MatchingHandler<EventT> handler = new MatchingHandler<>();
    source.addHandler(handler);
    return handler;
  }

  public static <EventT> Matcher<? super MatchingHandler<? extends EventT>> noEvents() {
    return new TypeSafeDiagnosingMatcher<MatchingHandler<? extends EventT>>() {
      @Override
      protected boolean matchesSafely(MatchingHandler<? extends EventT> item, Description mismatchDescription) {
        if (item.events.isEmpty()) {
          return true;
        } else {
          mismatchDescription.appendText("events happened: " + item.events);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("no events");
      }
    };
  }

  public static <EventT> Matcher<? super MatchingHandler<? extends EventT>> anyEvents() {
    return new TypeSafeDiagnosingMatcher<MatchingHandler<? extends EventT>>() {
      @Override
      protected boolean matchesSafely(MatchingHandler<? extends EventT> item, Description mismatchDescription) {
        if (item.events.isEmpty()) {
          mismatchDescription.appendText("no events happened");
          return false;
        } else {
          return true;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("any events");
      }
    };
  }

  public static <EventT> Matcher<MatchingHandler<EventT>> singleEvent(final Matcher<? super EventT> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<MatchingHandler<EventT>>() {
      @Override
      protected boolean matchesSafely(MatchingHandler<EventT> item, Description mismatchDescription) {
        if (item.events.isEmpty()) {
          mismatchDescription.appendText("no events happened");
          return false;
        } else if (item.events.size() == 1) {
          EventT value = item.events.get(0);
          if (valueMatcher.matches(value)) {
            return true;
          } else {
            mismatchDescription.appendText("value ");
            valueMatcher.describeMismatch(value, mismatchDescription);
            return false;
          }
        } else {
          mismatchDescription.appendText("few events happened: " + item.events);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("only event ").appendDescriptionOf(valueMatcher);
      }
    };
  }

  public static <EventT> Matcher<MatchingHandler<EventT>> lastEvent(final Matcher<? super EventT> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<MatchingHandler<EventT>>() {
      @Override
      protected boolean matchesSafely(MatchingHandler<EventT> item, Description mismatchDescription) {
        if (item.events.isEmpty()) {
          mismatchDescription.appendText("no events happened");
          return false;
        } else {
          EventT value = item.events.get(item.events.size() - 1);
          if (valueMatcher.matches(value)) {
            return true;
          } else {
            mismatchDescription.appendText("last value ");
            valueMatcher.describeMismatch(value, mismatchDescription);
            return false;
          }
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("last event ").appendDescriptionOf(valueMatcher);
      }
    };
  }

  public static <EventT> Matcher<MatchingHandler<? extends EventT>> allEvents(final Matcher<? super List<? extends EventT>> valuesMatcher) {
    return new TypeSafeDiagnosingMatcher<MatchingHandler<? extends EventT>>() {
      @Override
      protected boolean matchesSafely(MatchingHandler<? extends EventT> item, Description mismatchDescription) {
        if (valuesMatcher.matches(item.events)) {
          return true;
        } else {
          mismatchDescription.appendText("handled events ");
          valuesMatcher.describeMismatch(item.events, mismatchDescription);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("events ").appendDescriptionOf(valuesMatcher);
      }
    };
  }

  public static <ValueT> Matcher<PropertyChangeEvent<ValueT>> newValue(final Matcher<? super ValueT> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<PropertyChangeEvent<ValueT>>() {
      @Override
      protected boolean matchesSafely(PropertyChangeEvent<ValueT> item, Description mismatchDescription) {
        if (valueMatcher.matches(item.getNewValue())) {
          return true;
        } else {
          mismatchDescription.appendText("new value ");
          valueMatcher.describeMismatch(item.getNewValue(), mismatchDescription);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("new value ").appendDescriptionOf(valueMatcher);
      }
    };
  }

  public static <ValueT> Matcher<PropertyChangeEvent<ValueT>> newValueIs(ValueT value) {
    return newValue(CoreMatchers.is(value));
  }

  public static <ValueT> Matcher<PropertyChangeEvent<ValueT>> oldValue(final Matcher<? super ValueT> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<PropertyChangeEvent<ValueT>>() {
      @Override
      protected boolean matchesSafely(PropertyChangeEvent<ValueT> item, Description mismatchDescription) {
        if (valueMatcher.matches(item.getOldValue())) {
          return true;
        } else {
          mismatchDescription.appendText("old value ");
          valueMatcher.describeMismatch(item.getOldValue(), mismatchDescription);
          return false;
        }
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("old value ").appendDescriptionOf(valueMatcher);
      }
    };
  }

  public static <ValueT> Matcher<PropertyChangeEvent<ValueT>> oldValueIs(ValueT value) {
    return oldValue(CoreMatchers.is(value));
  }
}