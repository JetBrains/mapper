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

import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThrowableHandlersTest extends BaseTestCase {

  @Test(expected = IllegalStateException.class)
  public void addThrowingThrowableHandler() {
    Registration reg = ThrowableHandlers.addHandler(new Consumer<Throwable>() {
      @Override
      public void accept(Throwable event) {
        throw new IllegalStateException();
      }
    });
    try {
      ThrowableHandlers.asInProduction(new Runnable() {
        @Override
        public void run() {
          ThrowableHandlers.handle(new IllegalArgumentException());
        }
      });
    } finally {
      reg.remove();
    }
  }

  @Test
  public void removeHandlerWhileFire() {
    final Value<Registration> reg = new Value<>(Registration.EMPTY);
    reg.set(ThrowableHandlers.addHandler(new Consumer<Throwable>() {
      @Override
      public void accept(Throwable event) {
        reg.get().remove();
      }
    }));
    int handlersSize = ThrowableHandlers.getHandlersSize();
    ThrowableHandlers.asInProduction(new Runnable() {
      @Override
      public void run() {
        ThrowableHandlers.handle(new RuntimeException());
      }
    });
    assertEquals(handlersSize - 1, ThrowableHandlers.getHandlersSize());
  }
}