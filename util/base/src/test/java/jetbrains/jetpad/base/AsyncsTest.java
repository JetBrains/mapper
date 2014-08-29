/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AsyncsTest {
  @Test
  public void constantAsync() {
    Async<Integer> c = Asyncs.constant(239);
    assertAsyncEquals(c, 239);
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
    assertAsyncEquals(mapped, 240);
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

  @Test
  public void select() {
    Async<Integer> c = Asyncs.constant(239);
    Async<Integer> selected = Asyncs.select(c, new Function<Integer, Async<Integer>>() {
      @Override
      public Async<Integer> apply(Integer input) {
        return Asyncs.constant(input + 1);
      }
    });

    assertAsyncEquals(selected, 240);
  }

  @Test
  public void selectFirstFailure() {
    Async<Integer> failure = Asyncs.failure(new Throwable());
    assertFailure(Asyncs.select(failure, new Function<Integer, Async<Integer>>() {
      @Override
      public Async<Integer> apply(Integer input) {
        return Asyncs.constant(input + 1);
      }
    }));
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
    assertAsyncEquals(Asyncs.select(async, new Function<Integer, Async<Object>>() {
      @Override
      public Async<Object> apply(Integer input) {
        return null;
      }
    }), null);
  }

  @Test
  public void parallelSuccess() {
    assertAsyncEquals(Asyncs.parallel(Asyncs.constant(1), Asyncs.constant(2)), null);
  }

  @Test
  public void parallelFailure() {
    assertAsyncEquals(Asyncs.parallel(Asyncs.constant(1), Asyncs.failure(new Throwable())), null);
  }

  @Test
  public void untilSuccess() {
    assertAsyncEquals(Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
      @Override
      public Async<Integer> get() {
        return Asyncs.<Integer>constant(1);
      }
    }), 1);
  }

  @Test
  public void untilSuccessWithFailures() {
    assertAsyncEquals(Asyncs.untilSuccess(new Supplier<Async<Integer>>() {
      private int myCounter;

      @Override
      public Async<Integer> get() {
        if (myCounter++ < 10) {
          return Asyncs.<Integer>failure(new RuntimeException());
        }
        return Asyncs.<Integer>constant(1);
      }
    }), 1);
  }

  @Test
  public void checkedCast() {
    Async<String> stringAsync = Asyncs.constant("");
    Async<Object> objectAsync = Asyncs.<Object>checkedCast(stringAsync);
  }

  @Test
  public void loadedTrue() {
    assertTrue(Asyncs.isLoaded(Asyncs.constant(239)));
  }

  @Test
  public void loadedFalse() {
    SimpleAsync<Integer> async = new SimpleAsync<>();

    assertFalse(Asyncs.isLoaded(async));
  }


  private void assertFailure(Async<?> async) {
    final Value<Boolean> called = new Value<>(false);
    async.onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        called.set(true);
      }
    });

    assertTrue(called.get());
  }

  private <ValueT> void assertAsyncEquals(Async<ValueT> async, ValueT value) {
    final Value<ValueT> result = new Value<>();
    async.onSuccess(new Handler<ValueT>() {
      @Override
      public void handle(ValueT item) {
        result.set(item);
      }
    });

    assertEquals(result.get(), value);
  }
}