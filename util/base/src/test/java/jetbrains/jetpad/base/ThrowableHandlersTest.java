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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ThrowableHandlersTest extends BaseTestCase {

  @Test
  public void handleClientException() {
    ThrowableHandlers.handleError(createGwtException());
  }

  @Test
  public void handleClientError() {
    ThrowableHandlers.handleError(createGwtError());
  }

  @Test
  public void handleClientInnerError() {
    ThrowableHandlers.handleError(new RuntimeException(createGwtError()));
  }

  @Test
  public void handleServerException() {
    ThrowableHandlers.handleError(new RuntimeException());
  }

  @Test(expected = NoClassDefFoundError.class)
  public void handleServerError() {
    ThrowableHandlers.handleError(new NoClassDefFoundError());
  }

  @Test(expected = NoClassDefFoundError.class)
  public void handleServerInnerError() {
    ThrowableHandlers.handleError(new RuntimeException(new NoClassDefFoundError()));
  }

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


  private Exception createGwtException() {
    Exception e = new ClassCastException();
    e.setStackTrace(getStackTrace());

    return e;
  }

  private Error createGwtError() {
    Error e = new NoClassDefFoundError();
    e.setStackTrace(getStackTrace());

    return e;
  }

  private StackTraceElement[] getStackTrace() {
    List<StackTraceElement> stackTrace = new ArrayList<>();
    stackTrace.add(new StackTraceElement("jetbrains.jetpad.base.ThrowableHandlersTest", "createGwtException",
        "ThrowableHandlersTest.java", 100));
    stackTrace.add(new StackTraceElement("com.google.gwt.core.client.impl.Impl", "apply", "Impl.java", 244));
    stackTrace.add(new StackTraceElement("Unknown", "anonymous", "blob", 0));

    return stackTrace.toArray(new StackTraceElement[0]);
  }
}