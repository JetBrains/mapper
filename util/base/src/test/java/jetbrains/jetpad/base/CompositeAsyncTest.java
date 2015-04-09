/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompositeAsyncTest {
  private static final int SIZE = 2;

  @Test
  public void successOneByOne() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    Value<Boolean> succeeded = addSuccessHandler(async);

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).success(i);
      assertFalse(succeeded.get());
    }

    ((SimpleAsync<Integer>)asyncs.get(SIZE - 1)).success(SIZE - 1);
    assertTrue(succeeded.get());
  }

  @Test
  public void alreadySucceeded() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(Asyncs.constant(i));
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);
    Value<Boolean> succeeded = addSuccessHandler(async);
    assertTrue(succeeded.get());
  }

  @Test
  public void emptyRequest() {
    Async<List<Integer>> async = Asyncs.composite(new ArrayList<Async<Integer>>(0));
    final Value<Boolean> succeeded = new Value<>(false);
    async.onResult(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        succeeded.set(true);
        assertTrue(item.isEmpty());
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        throw new RuntimeException(item);
      }
    });
    assertTrue(succeeded.get());
  }

  @Test
  public void partialFailureSingleException() {
    List<Async<Integer>> asyncs = new ArrayList<>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    final Value<Boolean> failed = new Value<>(false);
    async.onResult(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        throw new UnsupportedOperationException();
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        failed.set(true);
        assertTrue(item instanceof IllegalStateException);
        assertEquals("test", item.getMessage());
      }
    });

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).success(i);
      assertFalse(failed.get());
    }

    ((SimpleAsync<Integer>)asyncs.get(SIZE - 1)).failure(new IllegalStateException("test"));
    assertTrue(failed.get());
  }

  @Test
  public void partialFailureSeveralExceptions() {
    List<Async<Integer>> asyncs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    Async<List<Integer>> async = Asyncs.composite(asyncs);

    final Value<Boolean> failed = new Value<>(false);
    async.onResult(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        throw new UnsupportedOperationException();
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        failed.set(true);
        List<Throwable> throwables = ((ThrowableCollectionException) item).getThrowables();
        assertEquals(2, throwables.size());
        List<String> expected = Arrays.asList("0", "1");
        List<String> actual = Arrays.asList(throwables.get(0).getMessage(), throwables.get(1).getMessage());
        assertEquals(expected, actual);
      }
    });

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).failure(new IllegalStateException("" + i));
      assertFalse(failed.get());
    }
    ((SimpleAsync<Integer>)asyncs.get(2)).success(2);
    assertTrue(failed.get());
  }

  private Value<Boolean> addSuccessHandler(Async<List<Integer>> async) {
    final Value<Boolean> succeeded = new Value<>(false);
    async.onResult(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        succeeded.set(true);
        assertEquals(SIZE, item.size());
        for (int i = 0; i < SIZE; i++) {
          assertTrue(item.contains(i));
        }
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        throw new RuntimeException(item);
      }
    });
    return succeeded;
  }

  @Test
  public void resultListOrder() {
    List<Async<Integer>> asyncs = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }

    final Value<Boolean> ok = new Value<>(false);
    Asyncs.composite(asyncs).onSuccess(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        for (int i = 0; i < item.size(); i++) {
          assertEquals(i, (int) item.get(i));
        }
        ok.set(true);
      }
    });

    ((SimpleAsync<Integer>)asyncs.get(1)).success(1);
    ((SimpleAsync<Integer>)asyncs.get(2)).success(2);
    ((SimpleAsync<Integer>)asyncs.get(0)).success(0);

    assertTrue(ok.get());
  }
}