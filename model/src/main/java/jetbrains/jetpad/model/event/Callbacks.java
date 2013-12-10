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
package jetbrains.jetpad.model.event;


import jetbrains.jetpad.base.ThrowableCollectionException;

import java.util.ArrayList;
import java.util.List;

public class Callbacks {
  public static interface Caller<CallbackT> {
    void call(CallbackT callback);
  }

  public static <CallbackT> void call(Iterable<CallbackT> callbacks, Caller<CallbackT> caller) {
    List<Throwable> exceptions = new ArrayList<Throwable>();
    for (final CallbackT c : callbacks) {
      try {
        caller.call(c);
      } catch (Throwable t) {
        exceptions.add(t);
      }
    }
    if (!exceptions.isEmpty()) {
      if (exceptions.size() == 1) {
        throw new RuntimeException(exceptions.get(0));
      }
      throw new ThrowableCollectionException(exceptions);
    }
  }
}