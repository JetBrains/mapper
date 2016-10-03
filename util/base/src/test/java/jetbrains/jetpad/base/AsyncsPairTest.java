package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AsyncsPairTest extends BaseTestCase {
  private SimpleAsync<Integer> first = new SimpleAsync<>();
  private SimpleAsync<String> second = new SimpleAsync<>();

  private Async<Pair<Integer, String>> pair;
  private Registration initReg;
  private Pair<Integer, String> result;
  private Throwable error;

  @Before
  public void init() {
    initPair(first, second);
  }

  private void initPair(Async<Integer> first, Async<String> second) {
    pair = Asyncs.pair(first, second);
    initReg = pair.onResult(
      item -> result = item,
      item -> error = item);
  }

  @Test
  public void successFirstEarlier() {
    first.success(1);

    assertNull(result);

    second.success("a");

    assertSucceeded();
  }

  @Test
  public void successSecondEarlier() {
    second.success("a");

    assertNull(result);

    first.success(1);

    assertSucceeded();
  }

  @Test
  public void successWithNulls() {
    first.success(null);
    second.success(null);

    assertNotNull(result);
    assertNull(result.first);
    assertNull(result.second);
  }

  @Test
  public void successFirstThenFailure() {
    first.success(1);
    Throwable throwable = new Throwable();
    second.failure(throwable);

    assertSame(throwable, error);
  }

  @Test
  public void successSecondThenFailure() {
    second.success("a");
    Throwable throwable = new Throwable();
    first.failure(throwable);

    assertSame(throwable, error);
  }

  @Test
  public void failureFirstThenSuccessSecond() {
    first.failure(new Throwable());
    second.success("a");

    assertNotNull(error);
  }

  @Test
  public void failureSecondThenSuccessFirst() {
    second.failure(new Throwable());
    first.success(1);

    assertNotNull(error);
  }

  @Test
  public void doubleFailureFirstEarlier() {
    first.failure(new Throwable());
    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void doubleFailureSecondEarlier() {
    second.failure(new Throwable());
    first.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successThenFirstAlreadySucceeded() {
    initReg.remove();
    initPair(Asyncs.constant(1), second);

    second.success("a");

    assertSucceeded();
  }

  @Test
  public void failureThenFirstAlreadySucceeded() {
    initReg.remove();
    initPair(Asyncs.constant(1), second);

    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successThenSecondAlreadySucceeded() {
    initReg.remove();
    initPair(first, Asyncs.constant("a"));

    first.success(1);

    assertSucceeded();
  }

  @Test
  public void failureThenSecondAlreadySucceeded() {
    initReg.remove();
    initPair(first, Asyncs.constant("a"));

    first.failure(new Throwable());

    assertNotNull(error);
  }


  @Test
  public void successSecondThenFirstAlreadyFailed() {
    initReg.remove();
    initPair(Asyncs.<Integer>failure(new Throwable()), second);

    second.success("a");

    assertNotNull(error);
  }

  @Test
  public void failureSecondThenFirstAlreadyFailed() {
    initReg.remove();
    initPair(Asyncs.<Integer>failure(new Throwable()), second);

    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successFirstThenSecondAlreadyFailed() {
    initReg.remove();
    initPair(first, Asyncs.<String>failure(new Throwable()));

    first.success(1);

    assertNotNull(error);
  }

  @Test
  public void failureFirstThenSecondAlreadyFailed() {
    initReg.remove();
    initPair(first, Asyncs.<String>failure(new Throwable()));

    first.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void bothSucceeded() {
    initReg.remove();
    initPair(Asyncs.constant(1), Asyncs.constant("a"));

    assertSucceeded();
  }

  @Test
  public void firstSucceededSecondFailed() {
    initReg.remove();
    initPair(Asyncs.constant(1), Asyncs.<String>failure(new Throwable()));

    assertNotNull(error);
  }

  @Test
  public void firstFailedSecondSucceeded() {
    initReg.remove();
    initPair(Asyncs.<Integer>failure(new Throwable()), Asyncs.constant("a"));

    assertNotNull(error);
  }

  @Test
  public void bothFailed() {
    initReg.remove();
    initPair(Asyncs.<Integer>failure(new Throwable()), Asyncs.<String>failure(new Throwable()));

    assertNotNull(error);
  }

  private void assertSucceeded() {
    assertNotNull(result);
    assertEquals(new Integer(1), result.first);
    assertEquals("a", result.second);
  }
}
