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

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import jetbrains.jetpad.base.function.Consumer;

import static org.junit.Assert.assertTrue;

public class SimpleAsyncTest extends BaseTestCase {
  private SimpleAsync<Void> async = new SimpleAsync<>();

  @Test
  public void exceptionInSuccessHandler() {
    async.onSuccess(throwingHandler());
    try {
      async.success(null);
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasSucceeded());
  }

  @Test
  public void exceptionInFailureHandler() {
    async.onFailure(this.<Throwable>throwingHandler());
    try {
      async.failure(new Throwable());
    } catch (RuntimeException e) {
    }
    assertTrue(async.hasFailed());
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInSuccessHandler() {
    async.onSuccess(succeedingHandler(async));
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callSuccessInFailureHandler() {
    async.onFailure(this.<Throwable>succeedingHandler(async));
    async.failure(new Throwable());
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInSuccessHandler() {
    async.onSuccess(failingHandler(async));
    async.success(null);
  }

  @Test(expected = IllegalStateException.class)
  public void callFailureInFailureHandler() {
    async.onFailure(this.<Throwable>failingHandler(async));
    async.failure(new Throwable());
  }

  private <ResultT> Consumer<ResultT> throwingHandler() {
    return new Consumer<ResultT>() {
      @Override
      public void accept(ResultT item) {
        throw new RuntimeException();
      }
    };
  }

  private <ResultT> Consumer<ResultT> succeedingHandler(final SimpleAsync<Void> async) {
    return new Consumer<ResultT>() {
      @Override
      public void accept(ResultT item) {
        async.success(null);
      }
    };
  }

  private <ResultT> Consumer<ResultT> failingHandler(final SimpleAsync<Void> async) {
    return new Consumer<ResultT>() {
      @Override
      public void accept(ResultT item) {
        async.failure(new Throwable());
      }
    };
  }
}