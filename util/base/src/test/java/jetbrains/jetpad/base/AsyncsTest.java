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
import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.base.function.Supplier;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static jetbrains.jetpad.base.AsyncMatchers.failed;
import static jetbrains.jetpad.base.AsyncMatchers.result;
import static jetbrains.jetpad.base.AsyncMatchers.voidSuccess;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AsyncsTest {
  @Test
  public void constantAsync() {
    assertThat(Asyncs.constant(239), result(equalTo(239)));
  }

  @Test
  public void failureAsync() {
    assertThat(Asyncs.<Integer>failure(new Throwable()), failed());
  }

  @Test
  public void internalMap() {
    Async<Integer> c = Asyncs.constant(239);
    Async<Integer> mapped = Asyncs.map(c, new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        return input + 1;
      }
    }, new SimpleAsync<Integer>());
    assertThat(mapped, result(equalTo(240)));
  }

  @Test
  public void map() {
    Async<Integer> c = Asyncs.constant(239);
    Async<Integer> mapped = c.map(new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        return input + 1;
      }
    });
    assertThat(mapped, result(equalTo(240)));
  }

  @Test
  public void internalMapFailure() {
    Async<Integer> failure = Asyncs.failure(new Throwable());
    Async<Integer> mapped = Asyncs.map(failure, new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        return input + 1;
      }
    }, new SimpleAsync<Integer>());
    assertThat(mapped, failed());
  }

  @Test
  public void mapFailure() {
    Async<Integer> a = Asyncs.constant(1);
    Async<Integer> mapped = a.map(new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer i) {
        throw new RuntimeException("test");
      }
    });
    assertThat(mapped, failed());
  }

  @Test(expected = IllegalArgumentException.class)
  public void ignoreHandlerException() {
    SimpleAsync<Integer> async = new SimpleAsync<>();
    Async<Integer> res = async.map(new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        return input + 1;
      }
    });
    res.onSuccess(new Consumer<Integer>() {
      @Override
      public void accept(Integer item) {
        throw new IllegalArgumentException();
      }
    });
    res.onFailure(new Consumer<Throwable>() {
      @Override
      public void accept(Throwable item) {
        fail();
      }
    });
    async.success(1);
  }

  @Test
  public void select() {
    Async<Integer> c = Asyncs.constant(239);
    assertThat(
        c.flatMap(new Function<Integer, Async<Integer>>() {
          @Override
          public Async<Integer> apply(Integer input) {
            return Asyncs.constant(input + 1);
          }
        }),
        result(equalTo(240)));
  }

  @Test
  public void selectException() {
    Async<Integer> a = Asyncs.constant(1);
    assertThat(
        a.flatMap(new Function<Integer, Async<Object>>() {
          @Override
          public Async<Object> apply(Integer input) {
            throw new RuntimeException("test");
          }
        }),
        failed());
  }

  @Test
  public void selectFirstFailure() {
    Async<Integer> failure = Asyncs.failure(new Throwable());
    assertThat(
        failure.flatMap(new Function<Integer, Async<Integer>>() {
          @Override
          public Async<Integer> apply(Integer input) {
            return Asyncs.constant(input + 1);
          }
        }),
        failed());
  }

  @Test
  public void selectReturnedFailure() {
    Async<Integer> async = Asyncs.constant(1);
    assertThat(
        async.flatMap(new Function<Integer, Async<Integer>>() {
          @Override
          public Async<Integer> apply(Integer input) {
            return Asyncs.failure(new Throwable());
          }
        }),
        failed());
  }

  @Test
  public void selectReturnsNull() {
    Async<Integer> async = Asyncs.constant(1);
    assertThat(
        async.flatMap(new Function<Integer, Async<Object>>() {
          @Override
          public Async<Object> apply(Integer input) {
            return null;
          }
        }),
        result(nullValue()));
  }

  @Test
  public void parallelSuccess() {
    Async<Void> parallel = Asyncs.parallel(Asyncs.constant(1), Asyncs.constant(2));
    assertThat(parallel, voidSuccess());
  }

  @Test
  public void parallelFailure() {
    assertThat(
        Asyncs.parallel(Asyncs.constant(1), Asyncs.failure(new Throwable())),
        failed());
  }

  @Test
  public void parallelAlwaysSucceed() {
    assertThat(
        Asyncs.parallel(Arrays.asList(Asyncs.constant(1), Asyncs.failure(new Throwable())), true),
        voidSuccess());
  }

  @Test
  public void emptyParallel() {
    assertThat(Asyncs.parallel(), voidSuccess());
  }

  @Test
  public void untilSuccess() {
    assertThat(
        Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
          @Override
          public Async<Integer> get() {
            return Asyncs.constant(1);
          }
        }),
        result(equalTo(1)));
  }

  @Test
  public void untilSuccessException() {
    assertThat(
        Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
          private int myCntr = 0;
          @Override
          public Async<Integer> get() {
            myCntr++;
            if (myCntr < 2) {
              throw new RuntimeException();
            } else {
              return Asyncs.constant(myCntr);
            }
          }
        }),
        result(equalTo(2)));
  }

  @Test
  public void untilSuccessWithFailures() {
    assertThat(
        Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
          private int myCounter;

          @Override
          public Async<Integer> get() {
            if (myCounter++ < 10) {
              return Asyncs.failure(new RuntimeException());
            }
            return Asyncs.constant(1);
          }
        }),
        result(equalTo(1)));
  }

  @Test
  public void getFailure() {
    assertThrow(new IllegalStateException("test"));
    assertThrow(new AssertionError("test"));
    assertThrowCause(new IOException("test"));
    assertThrowCause(new Throwable("test"));
  }

  private void assertThrowCause(Throwable throwable) {
    try {
      Asyncs.get(Asyncs.failure(throwable));
      fail("get expected to fail");
    } catch (Throwable t) {
      assertSame(throwable, t.getCause());
    }
  }

  private void assertThrow(Throwable throwable) {
    try {
      Asyncs.get(Asyncs.failure(throwable));
      fail("get expected to fail");
    } catch (Throwable t) {
      assertSame(throwable, t);
    }
  }
}