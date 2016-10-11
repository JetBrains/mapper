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

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Asynchronous computation
 * You must eventually call either succeedHandler or failureHandler.
 * If you imply a condition on handlers calls (i.e. synchronization)
 * you should imply the same conditions on map/flatMap handlers, and vice versa.
 */
public interface Async<ItemT> {
  Registration onSuccess(Consumer<? super ItemT> successHandler);
  Registration onResult(Consumer<? super ItemT> successHandler, Consumer<Throwable> failureHandler);
  Registration onFailure(Consumer<Throwable> failureHandler);

  /**
   * This method must always create new async every time it's called.
   * Every error thrown in {@code success} should fail async with corresponding {@code Throwable}
   */
  <ResultT> Async<ResultT> map(Function<? super ItemT, ? extends ResultT> success);

  /**
   * Should comply with A+ promise 'then' method except it has no failure handler.
   * See <a href="https://promisesaplus.com/">A+ promise spec</a> for more detail.
   * This method must always create new async every time it's called.
   * Every error thrown in {@code success} should fail async with corresponding {@code Throwable}
   */
  <ResultT> Async<ResultT> flatMap(Function<? super ItemT, Async<ResultT>> success);
}
