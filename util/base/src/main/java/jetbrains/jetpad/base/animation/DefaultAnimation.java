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
package jetbrains.jetpad.base.animation;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.SimpleAsync;

public abstract class DefaultAnimation implements Animation {
  private SimpleAsync<Object> myWhenDone = new SimpleAsync<>();
  private boolean myDone;

  protected abstract void doStop();

  public void done() {
    if (myDone) {
      throw new IllegalStateException();
    }
    myWhenDone.success(null);
    myDone = true;
  }

  @Override
  public void stop() {
    if (myDone) {
      throw new IllegalStateException();
    }
    doStop();
  }

  @Override
  public void whenDone(final Runnable r) {
    myWhenDone.handle(new Handler<Object>() {
      @Override
      public void handle(Object item) {
        r.run();
      }
    });
  }
}