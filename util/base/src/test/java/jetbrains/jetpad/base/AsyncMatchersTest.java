package jetbrains.jetpad.base;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static jetbrains.jetpad.base.AsyncMatchers.result;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class AsyncMatchersTest {

  @Test
  public void resultSucceeded() {
    try {
      assertThat(Asyncs.constant(239), result(is(238)));
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result is <238>\n" +
              "     but: result was <239>"
      ));
    }
  }

  @Test
  public void resultFailed() {
    try {
      assertThat(Asyncs.<Integer>failure(new Throwable()), result(is(0)));
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result is <0>\n" +
              "     but: failed"
      ));
    }
  }

  @Test
  public void resultUnfinished() {
    SimpleAsync<Integer> first = new SimpleAsync<>();
    SimpleAsync<Integer> second = new SimpleAsync<>();
    Async<List<Integer>> composite = Asyncs.composite(Arrays.<Async<Integer>>asList(first, second));
    first.success(0);
    try {
      assertThat(composite, AsyncMatchers.<List<Integer>>succeeded());
      fail();
    } catch (AssertionError e) {
      assertThat(e.getMessage(), is(
          "\n" +
              "Expected: a successful async which result ANYTHING\n" +
              "     but: isn't finished yet"
      ));
    }
  }

}
