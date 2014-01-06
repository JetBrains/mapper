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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompositeAsyncTest {
  private static final int SIZE = 2;

  @Test
  public void successOneByOne() {
    List<Async<Integer>> asyncs = new ArrayList<Async<Integer>>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    CompositeAsync<Integer> async = new CompositeAsync<Integer>(asyncs);

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
    List<Async<Integer>> asyncs = new ArrayList<Async<Integer>>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(Asyncs.constant(i));
    }
    CompositeAsync<Integer> async = new CompositeAsync<Integer>(asyncs);
    Value<Boolean> succeeded = addSuccessHandler(async);
    assertTrue(succeeded.get());
  }

  @Test
  public void emptyRequest() {
    CompositeAsync<Integer> async = new CompositeAsync<Integer>(new ArrayList<Async<Integer>>(0));
    final Value<Boolean> succeeded = new Value<Boolean>(false);
    async.onSuccess(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        succeeded.set(true);
        assertTrue(item.isEmpty());
      }
    }).onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        throw new RuntimeException(item);
      }
    });
    assertTrue(succeeded.get());
  }

  @Test
  public void partialFailure() {
    List<Async<Integer>> asyncs = new ArrayList<Async<Integer>>(SIZE);
    for (int i = 0; i < SIZE; i++) {
      asyncs.add(new SimpleAsync<Integer>());
    }
    CompositeAsync<Integer> async = new CompositeAsync<Integer>(asyncs);

    final Value<Boolean> failed = new Value<Boolean>(false);
    async.onSuccess(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        throw new UnsupportedOperationException();
      }
    }).onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        failed.set(true);
        List<Throwable> throwables = ((ThrowableCollectionException) item).getThrowables();
        assertEquals(1, throwables.size());
        assertEquals("test", throwables.get(0).getMessage());
      }
    });

    for (int i = 0; i < asyncs.size() - 1; i++) {
      ((SimpleAsync<Integer>)asyncs.get(i)).success(i);
      assertFalse(failed.get());
    }

    ((SimpleAsync<Integer>)asyncs.get(SIZE - 1)).failure(new IllegalStateException("test"));
    assertTrue(failed.get());
  }

  private Value<Boolean> addSuccessHandler(Async<List<Integer>> async) {
    final Value<Boolean> succeeded = new Value<Boolean>(false);
    async.onSuccess(new Handler<List<Integer>>() {
      @Override
      public void handle(List<Integer> item) {
        succeeded.set(true);
        assertEquals(SIZE, item.size());
        for (int i = 0; i < SIZE; i++) {
          assertTrue(item.contains(i));
        }
      }
    }).onFailure(new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        throw new RuntimeException(item);
      }
    });
    return succeeded;
  }
}