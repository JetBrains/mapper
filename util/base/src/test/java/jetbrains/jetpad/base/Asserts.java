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

/**
 * @deprecated Use {@link AsyncMatchers} instead
 */
@Deprecated
public final class Asserts {

  public static <T> T getResultValue(Async<T> async) {
    AsyncResult<T> result = getResult(async);
    if (result.state != AsyncState.SUCCEEDED) {
      throw new AssertionError("Async expected to succeed async=" + async + ", failure=" + result.error
          + ", state=" + result.state);
    }
    return result.value;
  }

  private static <T> AsyncResult<T> getResult(Async<T> async) {
    final Value<AsyncResult<T>> resultValue = new Value<>(new AsyncResult<T>(AsyncState.UNFINISHED, null, null));
    async.onResult(new Consumer<T>() {
      @Override
      public void accept(T value) {
        resultValue.set(new AsyncResult<>(AsyncState.SUCCEEDED, value, null));
      }
    }, new Consumer<Throwable>() {
      @Override
      public void accept(Throwable throwable) {
        resultValue.set(new AsyncResult<T>(AsyncState.FAILED, null, throwable));
      }
    });
    return resultValue.get();
  }

  private Asserts() {
  }

  private static class AsyncResult<T> {
    private final AsyncState state;
    private final T value;
    private final Throwable error;

    private AsyncResult(AsyncState state, T value, Throwable error) {
      this.state = state;
      this.value = value;
      this.error = error;
    }
  }

  private enum AsyncState {
    UNFINISHED,
    SUCCEEDED,
    FAILED
  }
}
