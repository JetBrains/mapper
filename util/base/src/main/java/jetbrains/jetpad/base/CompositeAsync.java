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

import java.util.*;

public class CompositeAsync<ItemT> extends SimpleAsync<List<ItemT>> {
  private SortedMap<Integer, ItemT> mySucceeded = new TreeMap<>();
  private List<Throwable> myFailures = new ArrayList<>(0);
  private int myAsyncsCounter = 0;
  private int mySuccessCounter = 0;

  public CompositeAsync(List<Async<ItemT>> asyncs) {
    myAsyncsCounter = asyncs.size();
    for (Async<ItemT> async : asyncs) {
      async.onSuccess(new Handler<ItemT>() {
        @Override
        public void handle(ItemT item) {
          mySucceeded.put(mySuccessCounter++, item);
          myAsyncsCounter--;
          onComponentResult();
        }
      }).onFailure(new Handler<Throwable>() {
        @Override
        public void handle(Throwable item) {
          myFailures.add(item);
          myAsyncsCounter--;
          onComponentResult();
        }
      });
    }

    if (asyncs.isEmpty()) {
      onComponentResult();
    }
  }

  private void onComponentResult() {
    if (myAsyncsCounter != 0) return;

    if (myFailures.isEmpty()) {
      success(new ArrayList<>(mySucceeded.values()));
    } else {
      if (myFailures.size() == 1) {
        failure(myFailures.get(0));
      } else {
        failure(new ThrowableCollectionException(myFailures));
      }
    }
  }
}