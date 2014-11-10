package jetbrains.jetpad.base;

import org.junit.Test;

import java.util.ConcurrentModificationException;

public class SimpleAsyncRegistrationsTest {
  private SimpleAsync<Void> async = new SimpleAsync<>();

  @Test
  public void removeSuccessRegistration() {
    Registration reg = async.onSuccess(throwingHandler());
    reg.remove();
    async.success(null);
  }

  @Test
  public void removeFailureRegistration() {
    Registration reg = async.onFailure(throwingFailureHandler());
    reg.remove();
    async.failure(null);
  }

  @Test
  public void removeCompositeRegistration1() {
    Registration reg = async.onResult(throwingHandler(), throwingFailureHandler());
    reg.remove();
    async.success(null);
  }

  @Test
  public void removeCompositeRegistration2() {
    Registration reg = async.onResult(throwingHandler(), throwingFailureHandler());
    reg.remove();
    async.failure(null);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeRegistrationInSuccessHandler() {
    final Value<Registration> regValue = new Value<>();
    Registration reg = async.onSuccess(new Handler<Void>() {
      @Override
      public void handle(Void item) {
        regValue.get().remove();
      }
    });
    regValue.set(reg);
    async.success(null);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeRegistrationInFailureHandler() {
    final Value<Registration> regValue = new Value<>();
    Registration reg = async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        regValue.get().remove();
      }
    });
    regValue.set(reg);
    async.failure(null);
  }

  @Test
  public void addSuccessHandlerAfterFailure() {
    async.failure(new Throwable());
    Registration reg = async.onSuccess(new Handler<Void>() {
      @Override
      public void handle(Void item) {
      }
    });
    reg.remove();
  }

  @Test
  public void addFailureHandlerAfterSuccess() {
    async.success(null);
    Registration reg = async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
      }
    });
    reg.remove();
  }

  private Handler<Throwable> throwingFailureHandler() {
    return throwingHandler();
  }

  private <ItemT> Handler<ItemT> throwingHandler() {
    return new Handler<ItemT>() {
      @Override
      public void handle(ItemT item) {
        throw new RuntimeException();
      }
    };
  }
}
