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

public final class ThreadSafeAsync<ItemT> implements Async<ItemT> {
  private final SimpleAsync<ItemT> myAsync;

  public ThreadSafeAsync() {
    myAsync = new SimpleAsync<>();
  }

  @Override
  public Registration onSuccess(Handler<? super ItemT> successHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onSuccess(successHandler));
    }
  }

  @Override
  public Registration onResult(Handler<? super ItemT> successHandler, Handler<Throwable> failureHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onResult(successHandler, failureHandler));
    }
  }

  @Override
  public Registration onFailure(Handler<Throwable> failureHandler) {
    synchronized (myAsync) {
      return safeReg(myAsync.onFailure(failureHandler)) ;
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

  public void success(ItemT item) {
    synchronized (myAsync) {
      myAsync.success(item);
    }
  }

  public void failure(Throwable throwable) {
    synchronized (myAsync) {
      myAsync.failure(throwable);
    }
  }
}