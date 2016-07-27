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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;

public class ExclusiveFilter {
  private boolean mySuspended = false;

  public <EventT> EventSource<EventT> exclusive(final EventSource<? extends EventT> source) {
    return new EventSource<EventT>() {
      @Override
      public Registration addHandler(final EventHandler<? super EventT> handler) {
        return source.addHandler(new EventHandler<EventT>() {
          @Override
          public void onEvent(final EventT event) {
            runExclusively(new Runnable() {
              @Override
              public void run() {
                handler.onEvent(event);
              }
            });
          }
        });
      }
    };
  }

  public void runExclusively(Runnable action) {
    if (!mySuspended) {
      mySuspended = true;
      try {
        action.run();
      } finally {
        mySuspended = false;
      }
    }
  }
}
