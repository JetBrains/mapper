package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.Test;

import static jetbrains.jetpad.base.AsyncMatchers.failed;
import static jetbrains.jetpad.base.AsyncMatchers.failure;
import static jetbrains.jetpad.base.AsyncMatchers.failureIs;
import static jetbrains.jetpad.base.AsyncMatchers.result;
import static jetbrains.jetpad.base.AsyncMatchers.resultEquals;
import static jetbrains.jetpad.base.AsyncMatchers.unfinished;
import static jetbrains.jetpad.base.AsyncMatchers.voidSuccess;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;


public class AsyncMatchersTest extends BaseTestCase {
  private static final String ASYNC_VALUE = "I am success";
  private static final String WRONG_VALUE = "Wrong value";

  @Test
  public void resultOnSuccessAsyncValueMatched() {
    assertMatches(successAsync(), result(equalTo(ASYNC_VALUE)));
  }

  @Test
  public void resultOnSuccessAsyncValueNotMatched() {
    assertNotMatches(successAsync(), result(equalTo(WRONG_VALUE)), ASYNC_VALUE);
  }

  @Test
  public void resultOnFailedAsync() {
    assertNotMatches(failedAsync(), result(equalTo(ASYNC_VALUE)));
  }

  @Test
  public void resultOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), result(equalTo(ASYNC_VALUE)));
  }


  @Test
  public void failureOnSuccessAsync() {
    assertNotMatches(successAsync(), failure(isA(TestAsyncException.class)));
  }

  @Test
  public void failureOnFailedAsyncValueMatches() {
    assertMatches(failedAsync(), failure(isA(TestAsyncException.class)));
  }

  @Test
  public void failureOnFailedAsyncValueNotMatches() {
    assertNotMatches(failedAsync(), failure(isA(NullPointerException.class)), TestAsyncException.class.getSimpleName());
  }

  @Test
  public void failureOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), failure(isA(NullPointerException.class)));
  }


  @Test
  public void unfinishedOnSuccessAsync() {
    assertNotMatches(successAsync(), unfinished());
  }

  @Test
  public void unfinishedOnFailedAsync() {
    assertNotMatches(failedAsync(), unfinished());
  }

  @Test
  public void unfinishedOnInProgressAsync() {
    assertMatches(inProgressAsync(), unfinished());
  }


  @Test
  public void resultEqualsOnSuccessAsyncValueMatched() {
    assertMatches(successAsync(), resultEquals(ASYNC_VALUE));
  }

  @Test
  public void resultEqualsOnSuccessAsyncValueNotMatched() {
    assertNotMatches(successAsync(), resultEquals(WRONG_VALUE), ASYNC_VALUE);
  }

  @Test
  public void resultEqualsOnFailedAsync() {
    assertNotMatches(failedAsync(), resultEquals(ASYNC_VALUE));
  }

  @Test
  public void resultEqualsOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), resultEquals(ASYNC_VALUE));
  }


  @Test
  public void succeededOnSuccessAsync() {
    assertMatches(successAsync(), AsyncMatchers.<String>succeeded());
  }

  @Test
  public void succeededOnFailedAsync() {
    assertNotMatches(failedAsync(), AsyncMatchers.<String>succeeded());
  }

  @Test
  public void succeededOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), AsyncMatchers.<String>succeeded());
  }


  @Test
  public void voidSuccessOnSuccessAsync() {
    assertMatches(voidSuccessAsync(), voidSuccess());
  }

  @Test
  public void voidSuccessOnFailedAsync() {
    SimpleAsync<Void> async = new SimpleAsync<>();
    async.failure(new TestAsyncException());
    assertNotMatches(async, voidSuccess());
  }

  @Test
  public void voidSuccessOnInProgressAsync() {
    assertNotMatches(new SimpleAsync<Void>(), voidSuccess());
  }


  @Test
  public void failureIsOnSuccessAsync() {
    assertNotMatches(successAsync(), failureIs(TestAsyncException.class));
  }

  @Test
  public void failureIsOnFailedAsyncValueMatches() {
    assertMatches(failedAsync(), failureIs(TestAsyncException.class));
  }

  @Test
  public void failureIsOnFailedAsyncValueNotMatches() {
    assertNotMatches(failedAsync(), failureIs(NullPointerException.class), TestAsyncException.class.getSimpleName());
  }

  @Test
  public void failureIsOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), failureIs(TestAsyncException.class));
  }


  @Test
  public void failedOnSuccessAsync() {
    assertNotMatches(successAsync(), failed());
  }

  @Test
  public void failedOnFailedAsync() {
    assertMatches(failedAsync(), failed());
  }

  @Test
  public void failedOnInProgressAsync() {
    assertNotMatches(inProgressAsync(), failed());
  }


  private <MatchedT> void assertNotMatches(MatchedT object, Matcher<MatchedT> matcher) {
    assertNotMatches(object, matcher, "");
  }

  private <MatchedT> void assertNotMatches(MatchedT object, Matcher<MatchedT> matcher, String requiredSubstring) {
    Assert.assertFalse(matcher.matches(object));
    String description = describe(object, matcher);
    Assert.assertThat(description, containsString(requiredSubstring));
  }

  private <MatchedT> void assertMatches(MatchedT object, Matcher<MatchedT> matcher) {
    if (!matcher.matches(object)) {
      Assert.fail(describe("Expected matches, but was mismatch with description ", object, matcher));
    }
  }

  private <MatchedT> String describe(MatchedT object, Matcher<MatchedT> matcher) {
    return describe("", object, matcher);
  }

  private <MatchedT> String describe(String prefix, MatchedT object, Matcher<MatchedT> matcher) {
    Description description = new StringDescription();
    matcher.describeMismatch(object, description.appendText(prefix));
    return description.toString();
  }

  private Async<Void> voidSuccessAsync() {
    return successAsync(null);
  }

  private Async<String> successAsync() {
    return successAsync(ASYNC_VALUE);
  }

  private <ResultT> Async<ResultT> successAsync(ResultT value) {
    SimpleAsync<ResultT> async = new SimpleAsync<>();
    async.success(value);
    return async;
  }

  private Async<String> failedAsync() {
    SimpleAsync<String> async = new SimpleAsync<>();
    async.failure(new TestAsyncException());
    return async;
  }

  private Async<String> inProgressAsync() {
    return new SimpleAsync<>();
  }

  private static class TestAsyncException extends RuntimeException {
  }
}