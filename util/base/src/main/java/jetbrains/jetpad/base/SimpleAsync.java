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

import java.util.ArrayList;
import java.util.List;

public final class SimpleAsync<ItemT> implements ResolvableAsync<ItemT> {
  private ItemT mySuccessItem;
  private boolean mySucceeded = false;

  private Throwable myFailureThrowable;
  private boolean myFailed = false;

  private List<Consumer<? super ItemT>> mySuccessHandlers = new ArrayList<>();
  private List<Consumer<Throwable>> myFailureHandlers = new ArrayList<>();

  @Override
  public Registration onSuccess(final Consumer<? super ItemT> successHandler) {
    if (alreadyHandled()) {
      if (mySucceeded) {
        successHandler.accept(mySuccessItem);
      }
      return Registration.EMPTY;
    }
    mySuccessHandlers.add(successHandler);
    return new Registration() {
      @Override
      protected void doRemove() {
        if (mySuccessHandlers != null) {
          mySuccessHandlers.remove(successHandler);
        }
      }
    };
  }

  @Override
  public Registration onResult(Consumer<? super ItemT> successHandler, Consumer<Throwable> failureHandler) {
    final Registration successRegistration = onSuccess(successHandler);
    final Registration failureRegistration = onFailure(failureHandler);
    return new Registration() {
      @Override
      protected void doRemove() {
        successRegistration.remove();
        failureRegistration.remove();
      }
    };
  }

  @Override
  public Registration onFailure(final Consumer<Throwable> handler) {
    if (alreadyHandled()) {
      if (myFailed) {
        handler.accept(myFailureThrowable);
      }
      return Registration.EMPTY;
    }
    myFailureHandlers.add(handler);
    return new Registration() {
      @Override
      protected void doRemove() {
        if (myFailureHandlers != null) {
          myFailureHandlers.remove(handler);
        }
      }
    };
  }

  @Override
  public <ResultT> Async<ResultT> map(Function<? super ItemT, ? extends ResultT> success) {
    return Asyncs.map(this, success, new SimpleAsync<ResultT>());
  }

  @Override
  public <ResultT> Async<ResultT> flatMap(Function<? super ItemT, Async<ResultT>> success) {
    return Asyncs.select(this, success, new SimpleAsync<ResultT>());
  }

  @Override
  public void success(ItemT item) {
    if (alreadyHandled()) {
      throw new IllegalStateException("Async already completed");
    }
    mySuccessItem = item;
    mySucceeded = true;

    for (Consumer<? super ItemT> handler : mySuccessHandlers) {
      try {
        handler.accept(item);
      } catch (Exception e) {
        ThrowableHandlers.handle(e);
      }
    }
    clearHandlers();
  }

  @Override
  public void failure(Throwable throwable) {
    if (alreadyHandled()) {
      throw new IllegalStateException("Async already completed");
    }
    myFailureThrowable = throwable;
    myFailed = true;

    for (Consumer<Throwable> handler : myFailureHandlers) {
      try {
        handler.accept(throwable);
      } catch (Throwable t) {
        ThrowableHandlers.handle(t);
      }
    }
    clearHandlers();
  }

  private void clearHandlers() {
    mySuccessHandlers = null;
    myFailureHandlers = null;
  }

  private boolean alreadyHandled() {
    return mySucceeded || myFailed;
  }

  boolean hasSucceeded() {
    return mySucceeded;
  }

  boolean hasFailed() {
    return myFailed;
  }
}