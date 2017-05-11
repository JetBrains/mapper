package jetbrains.jetpad.base;

import jetbrains.jetpad.base.function.Consumer;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.core.IsAnything;

public class AsyncMatchers {
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
            mismatchDescription.appendText("failed");
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

  public static <T, E extends Throwable> Matcher<Async<T>> failure(final Matcher<? super E> failureMatcher) {
    return new TypeSafeDiagnosingMatcher<Async<T>>() {
      @Override
      protected boolean matchesSafely(Async<T> item, Description mismatchDescription) {
        AsyncResult<T> result = getResult(item);
        switch (result.state) {
          case SUCCEEDED:
            mismatchDescription.appendText("succeeded");
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

  public static <T> Matcher<Async<T>> unfinished() {
    return new TypeSafeDiagnosingMatcher<Async<T>>() {
      @Override
      protected boolean matchesSafely(Async<T> item, Description mismatchDescription) {
        AsyncResult<T> result = getResult(item);
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

  public static <T, E extends Throwable> Matcher<Async<T>> failureIs(Class<E> failureClass) {
    return failure(Matchers.isA(failureClass));
  }

  public static <T> Matcher<Async<T>> succeeded() {
    return result(new IsAnything<T>());
  }

  public static <T> Matcher<Async<T>> failed() {
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
