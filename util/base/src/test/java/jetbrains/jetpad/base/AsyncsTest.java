/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AsyncsTest {
  @Test
  public void constantAsync() {
    Async<Integer> c = Asyncs.constant(239);
    assertSuccess(c, 239);
  }

  @Test
  public void failureAsync() {
    assertFailure(Asyncs.<Integer>failure(new Throwable()));
  }

  @Test
  public void map() {
    Async<Integer> c = Asyncs.constant(239);
    Async<Integer> mapped = Asyncs.map(c, new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        return input + 1;
      }
    });
    assertSuccess(mapped, 240);
  }

  @Test
  public void mapFailure() {
    Async<Integer> failure = Asyncs.failure(new Throwable());
    assertFailure(Asyncs.map(failure, new Function<Integer, Object>() {
      @Override
      public Object apply(Integer input) {
        return input + 1;
      }
    }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void ignoreHandlerException() {
    SimpleAsync<Integer> async = new SimpleAsync<>();
    Async<Integer> res = async.map(input -> input + 1);
    res.onSuccess((Handler<Integer>) item -> {
      throw new IllegalArgumentException();
    });
    res.onFailure((Handler<Throwable>) item -> fail());
    async.success(1);
  }

  @Test
  public void mapException() {
    Async<Integer> a = Asyncs.constant(1);
    assertFailure(Asyncs.map(a, new Function<Integer, Integer>() {
      @Override
      public Integer apply(Integer input) {
        throw new RuntimeException("test");
      }
    }));
  }

  @Test
  public void select() {
    Async<Integer> c = Asyncs.constant(239);
    Async<Integer> selected = c.flatMap(input -> Asyncs.constant(input + 1));

    assertSuccess(selected, 240);
  }

  @Test
  public void selectException() {
    Async<Integer> a = Asyncs.constant(1);
    assertFailure(a.flatMap(input -> {
      throw new RuntimeException("test");
    }));
  }

  @Test
  public void selectFirstFailure() {
    Async<Integer> failure = Asyncs.failure(new Throwable());
    assertFailure(failure.flatMap(input -> Asyncs.constant(input + 1)));
  }

  @Test
  public void selectReturnedFailure() {
    Async<Integer> async = Asyncs.constant(1);
    assertFailure(Asyncs.select(async, new Function<Integer, Async<Integer>>() {
      @Override
      public Async<Integer> apply(Integer input) {
        return Asyncs.failure(new Throwable());
      }
    }));
  }

  @Test
  public void selectReturnsNull() {
    Async<Integer> async = Asyncs.constant(1);
    assertSuccessNull(async.flatMap(input -> null));
  }

  @Test
  public void parallelSuccess() {
    assertSuccessNull(Asyncs.parallel(Asyncs.constant(1), Asyncs.constant(2)));
  }

  @Test
  public void parallelFailure() {
    assertFailure(Asyncs.parallel(Asyncs.constant(1), Asyncs.failure(new Throwable())));
  }

  @Test
  public void parallelAlwaysSucceed() {
    assertSuccessNull(Asyncs.parallel(Arrays.asList(Asyncs.constant(1), Asyncs.failure(new Throwable())), true));
  }

  @Test
  public void emptyParallel() {
    assertSuccessNull(Asyncs.parallel());
  }

  @Test
  public void untilSuccess() {
    assertSuccess(Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
      @Override
      public Async<Integer> get() {
        return Asyncs.constant(1);
      }
    }), 1);
  }

  @Test
  public void untilSuccessException() {
    assertSuccess(Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
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
    }), 2);
  }

  @Test
  public void untilSuccessWithFailures() {
    assertSuccess(Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
      private int myCounter;

      @Override
      public Async<Integer> get() {
        if (myCounter++ < 10) {
          return Asyncs.failure(new RuntimeException());
        }
        return Asyncs.constant(1);
      }
    }), 1);
  }

  @Test
  public void succeededTrue() {
    assertTrue(Asyncs.isSucceeded(Asyncs.constant(239)));
  }

  @Test
  public void succeededFalse() {
    SimpleAsync<Integer> async = new SimpleAsync<>();

    assertFalse(Asyncs.isSucceeded(async));
  }

  @Test
  public void failedTrue() {
    assertTrue(Asyncs.isFailed(Asyncs.failure(new RuntimeException())));
  }

  @Test
  public void failedFalse() {
    assertFalse(Asyncs.isFailed(Asyncs.constant(null)));
  }

  private void assertFailure(Async<?> async) {
    final Value<Boolean> called = new Value<>(false);
    async.onFailure(item -> called.set(true));

    assertTrue(called.get());
  }

  private <ValueT> void assertSuccess(Async<ValueT> async, ValueT value) {
    if (value == null) {
      throw new IllegalStateException();
    }
    final Value<ValueT> result = new Value<>();
    async.onSuccess(result::set);
    assertEquals(result.get(), value);
  }

  private <ValueT> void assertSuccessNull(Async<ValueT> async) {
    final Value<Object> result = new Value<>(new Object());
    async.onSuccess(result::set);
    assertNull(result.get());
  }
}
