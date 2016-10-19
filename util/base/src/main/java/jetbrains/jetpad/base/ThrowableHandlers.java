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
import jetbrains.jetpad.base.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThrowableHandlers {
  private static final Logger LOG = Logger.getLogger(ThrowableHandlers.class.getName());

  //we can use ThreadLocal here because of our own emulation at model-gwt jetbrains.jetpad.model.jre.java.lang.ThreadLocal
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<Boolean> ourForceProduction = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return false;
    }
  };

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<MyEventSource> ourHandlers = new ThreadLocal<MyEventSource>() {
    @Override
    protected MyEventSource initialValue() {
      return new MyEventSource();
    }
  };

  public static Registration addHandler(Consumer<? super Throwable> handler) {
    return ourHandlers.get().addHandler(handler);
  }

  public static void asInProduction(Runnable r) {
    if (ourForceProduction.get()) {
      throw new IllegalStateException();
    }
    ourForceProduction.set(true);
    try {
      r.run();
    } finally {
      ourForceProduction.set(false);
    }
  }

  public static void handle(Throwable t) {
    if (isInUnitTests(t)) {
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t);
    }
    LOG.log(Level.SEVERE, "Exception handled at ThrowableHandlers", new RuntimeException(t));
    ourHandlers.get().fire(t);
  }

  private static boolean isInUnitTests(Throwable t) {
    if (ourForceProduction.get()) {
      return false;
    }
    for (StackTraceElement e : t.getStackTrace()) {
      if (e.getClassName().startsWith("org.junit.runners")) {
        return true;
      }
    }
    return false;
  }

  static int getHandlersSize() {
    return ourHandlers.get().size();
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