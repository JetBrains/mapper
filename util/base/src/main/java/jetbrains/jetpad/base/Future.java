/*
 * Copyright 2012-2013 JetBrains s.r.o
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

public class Future<ItemT> implements Async<ItemT> {
  private ItemT mySuccessItem = null;
  private boolean mySucceeded = false;

  private Throwable myFailureThrowable = null;
  private boolean myFailed = false;

  private List<Handler<? super ItemT>> mySuccessHandlers = new ArrayList<Handler<? super ItemT>>();
  private List<Handler<Throwable>> myFailureHandlers = new ArrayList<Handler<Throwable>>();

  public Async<ItemT> onSuccess(Handler<? super ItemT> handler) {
    if (mySucceeded) {
      handler.handle(mySuccessItem);
    } else {
      mySuccessHandlers.add(handler);
    }
    return this;
  }

  public Async<ItemT> onFailure(Handler<Throwable> handler) {
    if (myFailed) {
      handler.handle(myFailureThrowable);
    } else {
      myFailureHandlers.add(handler);
    }
    return this;
  }

  public void success(ItemT item) {
    if (alreadyHandled()) throw new IllegalStateException();

    for (Handler<? super ItemT> handler: mySuccessHandlers) {
      handler.handle(item);
    }
    clearHandlers();
    mySuccessItem = item;
    mySucceeded = true;
  }

  public void failure(Throwable throwable) {
    if (alreadyHandled()) throw new IllegalStateException();

    for (Handler<Throwable> handler: myFailureHandlers) {
      handler.handle(throwable);
    }
    clearHandlers();
    myFailureThrowable = throwable;
    myFailed = true;
  }

  private void clearHandlers() {
    mySuccessHandlers.clear();
    myFailureHandlers.clear();
  }

  private boolean alreadyHandled() {
    return mySucceeded || myFailed;
  }
}

