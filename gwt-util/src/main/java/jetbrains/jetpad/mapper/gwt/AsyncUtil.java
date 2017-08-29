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
package jetbrains.jetpad.mapper.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;
import jetbrains.jetpad.base.SimpleAsync;

public final class AsyncUtil {
  public static <ItemT> AsyncCallback<ItemT> getCallback(final SimpleAsync<ItemT> async) {
    return new AsyncCallback<ItemT>() {
      @Override
      public void onFailure(Throwable caught) {
        async.failure(caught);
      }

      @Override
      public void onSuccess(ItemT result) {
        async.success(result);
      }
    };
  }

  private AsyncUtil() {
  }
}