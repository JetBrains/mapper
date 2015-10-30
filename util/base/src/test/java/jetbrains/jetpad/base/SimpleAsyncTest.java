package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleAsyncTest extends BaseTestCase {
  private SimpleAsync<Void> async = new SimpleAsync<>();

  @Test
  public void exceptionInSuccessHandler() {
    async.onSuccess(throwingHandler());
    try {
      async.success(null);
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasSucceeded());
  }

  @Test
  public void exceptionInFailureHandler() {
    async.onFailure(this.<Throwable>throwingHandler());
    try {
      async.failure(new Throwable());
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasFailed());
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInSuccessHandler() {
    async.onSuccess(succeedingHandler(async));
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInFailureHandler() {
    async.onFailure(this.<Throwable>succeedingHandler(async));
    async.failure(new Throwable());
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInSuccessHandler() {
    async.onSuccess(failingHandler(async));
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInFailureHandler() {
    async.onFailure(this.<Throwable>failingHandler(async));
    async.failure(new Throwable());
  }

  private <ResultT> Handler<ResultT> throwingHandler() {
    return new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        throw new RuntimeException();
      }
    };
  }

  private <ResultT> Handler<ResultT> succeedingHandler(final SimpleAsync<Void> async) {
    return new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        async.success(null);
      }
    };
  }

  private <ResultT> Handler<ResultT> failingHandler(final SimpleAsync<Void> async) {
    return new Handler<ResultT>() {
      @Override
      public void handle(ResultT item) {
        async.failure(new Throwable());
      }
    };
  }
}
