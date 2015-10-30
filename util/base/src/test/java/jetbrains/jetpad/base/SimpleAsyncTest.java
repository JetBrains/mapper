package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SimpleAsyncTest extends BaseTestCase {
  private SimpleAsync<Void> async = new SimpleAsync<>();

  @Test
  public void exceptionInSuccessHandler() {
    async.onSuccess(new Handler<Void>() {
      @Override
      public void handle(Void item) {
        throw new RuntimeException();
      }
    });
    try {
      async.success(null);
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasSucceeded());
  }

  @Test
  public void exceptionInFailureHandler() {
    async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable t) {
        throw new RuntimeException();
      }
    });
    try {
      async.failure(new Throwable());
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasFailed());
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInSuccessHandler() {
    async.onSuccess(new Handler<Void>() {
      @Override
      public void handle(Void item) {
        async.success(null);
      }
    });
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInFailureHandler() {
    async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        async.success(null);
      }
    });
    async.failure(new Throwable());
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInSuccessHandler() {
    async.onSuccess(new Handler<Void>() {
      @Override
      public void handle(Void item) {
        async.failure(null);
      }
    });
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInFailureHandler() {
    async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        async.failure(null);
      }
    });
    async.failure(new Throwable());
  }
}
