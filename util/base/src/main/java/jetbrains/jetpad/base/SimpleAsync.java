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

import java.util.ArrayList;
import java.util.List;

public final class SimpleAsync<ItemT> implements Async<ItemT> {
  private ItemT mySuccessItem = null;
  private boolean mySucceeded = false;

  private Throwable myFailureThrowable = null;
  private boolean myFailed = false;

  private List<Handler<? super ItemT>> mySuccessHandlers = new ArrayList<>();
  private List<Handler<Throwable>> myFailureHandlers = new ArrayList<>();

  @Override
  public Registration onSuccess(final Handler<? super ItemT> successHandler) {
    if (alreadyHandled()) {
      if (mySucceeded) {
        successHandler.handle(mySuccessItem);
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
  public Registration onResult(Handler<? super ItemT> successHandler, final Handler<Throwable> failureHandler) {
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
  public Registration onFailure(final Handler<Throwable> failureHandler) {
    if (alreadyHandled()) {
      if (myFailed) {
        failureHandler.handle(myFailureThrowable);
      }
      return Registration.EMPTY;
    }
    myFailureHandlers.add(failureHandler);
    return new Registration() {
      @Override
      protected void doRemove() {
        if (myFailureHandlers != null) {
          myFailureHandlers.remove(failureHandler);
        }
      }
    };
  }

  public void success(ItemT item) {
    if (alreadyHandled()) {
      throw new IllegalStateException();
    }
    mySuccessItem = item;
    mySucceeded = true;

    for (Handler<? super ItemT> handler : mySuccessHandlers) {
      try {
        handler.handle(item);
      } catch (Exception e) {
        ThrowableHandlers.handle(e);
      }
    }
    clearHandlers();
  }

  public void failure(Throwable throwable) {
    if (alreadyHandled()) {
      throw new IllegalStateException();
    }
    myFailureThrowable = throwable;
    myFailed = true;

    for (Handler<Throwable> handler : myFailureHandlers) {
      try {
        handler.handle(throwable);
      } catch (Exception e) {
        ThrowableHandlers.handle(e);
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