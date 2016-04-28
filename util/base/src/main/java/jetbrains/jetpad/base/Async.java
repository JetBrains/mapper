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

/**
 * Asynchronous computation
 */
public interface Async<ItemT> {
  Registration onSuccess(Handler<? super ItemT> successHandler);
  Registration onResult(Handler<? super ItemT> successHandler, Handler<Throwable> failureHandler);
  Registration onFailure(Handler<Throwable> failureHandler);

  /**
   * This method must always create new async every time it's called.
   *
   * @param success   handler to transform async result.
   * @param <ResultT> resulting async value type.
   * @return async that is fulfilled when parent async does.
   */
  <ResultT> Async<ResultT> map(Function<? super ItemT, ? extends ResultT> success);

  /**
   * Should comply with A+ promise 'then' method except it has no failure handler.
   * See <a href="https://promisesaplus.com/">A+ promise spec</a> for more detail.
   * This method must always create new async every time it's called.
   *
   * @param success   handler to pass parent async result to another async
   * @param <ResultT> resulting async value type
   * @return async that is fulfilled when async resulting from handler does
   */
  <ResultT> Async<ResultT> then(Function<? super ItemT, Async<ResultT>> success);
}