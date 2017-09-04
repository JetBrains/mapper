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

public final class ThreadSafeAsync<ItemT> implements ResolvableAsync<ItemT> {
  private final SimpleAsync<ItemT> myAsync;

  public ThreadSafeAsync() {
    myAsync = new SimpleAsync<>();
  }

  @Override
  public Registration onSuccess(Consumer<? super ItemT> successHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onSuccess(successHandler));
    }
  }

  @Override
  public Registration onResult(Consumer<? super ItemT> successHandler, Consumer<Throwable> failureHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onResult(successHandler, failureHandler));
    }
  }

  @Override
  public Registration onFailure(Consumer<Throwable> failureHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onFailure(failureHandler)) ;
    }
  }

  @Override
  public <ResultT> Async<ResultT> map(Function<? super ItemT, ? extends ResultT> success) {
    synchronized (myAsync) {
      return Asyncs.map(this, success, new ThreadSafeAsync<ResultT>());
    }
  }

  @Override
  public <ResultT> Async<ResultT> flatMap(Function<? super ItemT, Async<ResultT>> success) {
    synchronized (myAsync) {
      return Asyncs.select(this, success, new ThreadSafeAsync<ResultT>());
    }
  }

  private Registration safeReg(final Registration r) {
    return new Registration() {
      @Override
      protected void doRemove() {
        synchronized (myAsync) {
          r.remove();
        }
      }
    };
  }

  @Override
  public void success(ItemT item) {
    synchronized (myAsync) {
      myAsync.success(item);
    }
  }

  @Override
  public void failure(Throwable throwable) {
    synchronized (myAsync) {
      myAsync.failure(throwable);
    }
  }
}