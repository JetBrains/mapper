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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ThrowableHandlers {
  private static final Logger LOG = Logger.getLogger(ThrowableHandlers.class.getName());
  public static final boolean DEBUG = false;

  // we can use ThreadLocal here because of our own emulation
  // at model-gwt jetbrains.jetpad.model.jre.java.lang.ThreadLocal
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static final ThreadLocal<Boolean> FORCE_PRODUCTION = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.FALSE;
    }
  };

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static final ThreadLocal<Boolean> LOGGING_ENABLED = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return Boolean.TRUE;
    }
  };

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static final ThreadLocal<MyEventSource> HANDLERS = new ThreadLocal<MyEventSource>() {
    @Override
    protected MyEventSource initialValue() {
      return new MyEventSource();
    }
  };

  private final static Set<RuntimeException> LEAKS = Collections.newSetFromMap(new IdentityHashMap<RuntimeException, Boolean>());

  public static void setLoggingEnabled(boolean enabled) {
    LOGGING_ENABLED.set(enabled);
  }
  
  public static Registration addHandler(Consumer<? super Throwable> handler) {
    final Registration handlerReg = HANDLERS.get().addHandler(handler);

    final Value<Registration> leakReg = new Value<>();
    if (DEBUG) {
      final RuntimeException leakStacktrace = new RuntimeException("Potential leak");
      synchronized (LEAKS) {
        LEAKS.add(leakStacktrace);
      }
      leakReg.set(new Registration() {
        @Override
        protected void doRemove() {
          synchronized (LEAKS) {
            LEAKS.remove(leakStacktrace);
          }
        }
      });
    }

    return new Registration() {
      @Override
      protected void doRemove() {
        handlerReg.remove();
        if (leakReg.get() != null) {
          leakReg.get().dispose();
        }
      }
    };
  }

  public static void asInProduction(Runnable r) {
    if (FORCE_PRODUCTION.get()) {
      throw new IllegalStateException();
    }
    FORCE_PRODUCTION.set(Boolean.TRUE);
    try {
      r.run();
    } finally {
      FORCE_PRODUCTION.set(Boolean.FALSE);
    }
  }

  public static void handle(Throwable t) {
    if (isInUnitTests(t)) {
      if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      }
      throw new RuntimeException(t);
    }

    // ourForceProduction is used only in tests, where severe level is logged by default.
    if (!FORCE_PRODUCTION.get() && LOGGING_ENABLED.get()) {
      LOG.log(Level.SEVERE, "Exception handled at ThrowableHandlers", t);
    }
    handleError(t);
    HANDLERS.get().fire(t);
  }

  private static boolean isInUnitTests(Throwable t) {
    if (FORCE_PRODUCTION.get()) {
      return false;
    }
    for (StackTraceElement e : t.getStackTrace()) {
      if (e.getClassName().startsWith("org.junit.runners")) {
        return true;
      }
    }
    return false;
  }

  static void handleError(Throwable t) {
    if (isClient(t)) {
      return;
    }
    Error error = getError(t);
    if (error != null) {
      throw error;
    }
  }

  private static boolean isClient(Throwable t) {
    boolean isClient = false;
    StackTraceElement[] stackTrace = t.getStackTrace();

    for (int i = stackTrace.length - 1; !isClient && i >= 0; i--) {
      String className = stackTrace[i].getClassName();

      if (className.startsWith("com.google.gwt.core.client") || "Unknown".equals(className)) {
        isClient = true;
      }
    }

    if (!isClient && t.getCause() != null) {
      isClient = isClient(t.getCause());
    }

    return isClient;
  }

  private static Error getError(Throwable t) {
    Error error = (t instanceof Error) ? (Error) t : null;

    if (error == null && t.getCause() != null) {
      error = getError(t.getCause());
    }

    return error;
  }

  static int getHandlersSize() {
    return HANDLERS.get().size();
  }

  public static boolean checkForLeaks(PrintStream stream) {
    if (HANDLERS.get().size() == 0) {
      return false;
    }
    synchronized (LEAKS) {
      for (RuntimeException leak : LEAKS) {
        leak.printStackTrace(stream);
      }
    }
    return true;
  }

  private ThrowableHandlers() {
  }

  private static class MyEventSource {
    private final List<Consumer<? super Throwable>> myHandlers = new ArrayList<>();

    void fire(Throwable throwable) {
      for (Consumer<? super Throwable> handler : new ArrayList<>(myHandlers)) {
        handler.accept(throwable);
      }
    }

    Registration addHandler(final Consumer<? super Throwable> handler) {
      myHandlers.add(handler);
      return new Registration() {
        @Override
        protected void doRemove() {
          myHandlers.remove(handler);
        }
      };
    }

    int size() {
      return myHandlers.size();
    }
  }
}
