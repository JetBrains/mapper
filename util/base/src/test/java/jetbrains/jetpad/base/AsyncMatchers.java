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
package jetbrains.jetpad.base;

import jetbrains.jetpad.base.function.Consumer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public final class AsyncMatchers {
  public static <T> Matcher<Async<T>> result(final Matcher<? super T> valueMatcher) {
    return new TypeSafeDiagnosingMatcher<Async<T>>() {
      @Override
      protected boolean matchesSafely(Async<T> item, Description mismatchDescription) {
        AsyncResult<T> result = getResult(item);
        switch (result.state) {
          case SUCCEEDED:
            if (valueMatcher.matches(result.value)) {
              return true;
            } else {
              mismatchDescription.appendText("result ");
              valueMatcher.describeMismatch(result.value, mismatchDescription);
              return false;
            }
          case FAILED:
            mismatchDescription.appendText("failed with exception: ");
            mismatchDescription.appendValue(result.error);
            return false;
          case UNFINISHED:
            mismatchDescription.appendText("isn't finished yet");
            return false;
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a successful async which result ").appendDescriptionOf(valueMatcher);
      }
    };
  }

  public static <E extends Throwable> Matcher<Async<?>> failure(final Matcher<? super E> failureMatcher) {
    return new TypeSafeDiagnosingMatcher<Async<?>>() {
      @Override
      protected boolean matchesSafely(Async<?> item, Description mismatchDescription) {
        AsyncResult<?> result = getResult(item);
        switch (result.state) {
          case SUCCEEDED:
            mismatchDescription.appendText("was a successful async with value: ");
            mismatchDescription.appendValue(result.value);
            return false;
          case FAILED:
            if (failureMatcher.matches(result.error)) {
              return true;
            } else {
              mismatchDescription.appendText("failure ");
              failureMatcher.describeMismatch(result.error, mismatchDescription);
              return false;
            }
          case UNFINISHED:
            mismatchDescription.appendText("isn't finished yet");
            return false;
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a failed async which failure ").appendDescriptionOf(failureMatcher);
      }
    };
  }

  public static Matcher<Async<?>> unfinished() {
    return new TypeSafeDiagnosingMatcher<Async<?>>() {
      @Override
      protected boolean matchesSafely(Async<?> item, Description mismatchDescription) {
        AsyncResult<?> result = getResult(item);
        switch (result.state) {
          case SUCCEEDED:
            mismatchDescription.appendText("async succeeded");
            return false;
          case FAILED:
            mismatchDescription.appendText("async failed");
            return false;
          case UNFINISHED:
            return true;
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("an unfinished async");
      }
    };
  }

  public static <T> Matcher<Async<T>> resultEquals(T value) {
    return result(Matchers.equalTo(value));
  }

  public static <T> Matcher<Async<T>> succeeded() {
    return result(new IsAnything<T>());
  }

  public static Matcher<Async<Void>> voidSuccess() {
    return result(Matchers.nullValue());
  }

  public static <E extends Throwable> Matcher<Async<?>> failureIs(Class<E> failureClass) {
    return failure(Matchers.isA(failureClass));
  }

  public static Matcher<Async<?>> failed() {
    return failure(new IsAnything<>());
  }

  private static <T> AsyncResult<T> getResult(Async<T> async) {
    final Value<AsyncResult<T>> resultValue = new Value<>(new AsyncResult<T>(AsyncState.UNFINISHED, null, null));
    async.onResult(
        new Consumer<T>() {
          @Override
          public void accept(T value) {
            resultValue.set(new AsyncResult<>(AsyncState.SUCCEEDED, value, null));
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable value) {
            resultValue.set(new AsyncResult<T>(AsyncState.FAILED, null, value));
          }
        });
    return resultValue.get();
  }

  private AsyncMatchers() {
  }

  private static class AsyncResult<T> {
    private final AsyncState state;
    private final T value;
    private final Throwable error;

    private AsyncResult(AsyncState state, T value, Throwable error) {
      this.state = state;
      this.value = value;
      this.error = error;
    }
  }

  private enum AsyncState {
    UNFINISHED,
    SUCCEEDED,
    FAILED
  }
}
