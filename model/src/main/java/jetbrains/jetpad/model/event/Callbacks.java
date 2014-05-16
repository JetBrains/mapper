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
package jetbrains.jetpad.model.event;


public class Callbacks {
  //we can use ThreadLocal here because of our own emulation at jetbrains.jetpad.model.jre.java.lang.ThreadLocal
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<Boolean> ourForceProduction = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return false;
    }
  };

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<SimpleEventSource<Throwable>> ourCallbackExceptions = new ThreadLocal<SimpleEventSource<Throwable>>() {
    @Override
    protected SimpleEventSource<Throwable> initialValue() {
      return new SimpleEventSource<>();
    }
  };

  public static EventSource<Throwable> callbackExceptions() {
    return ourCallbackExceptions.get();
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

  public static void handleException(Throwable t) {
    if (isInUnitTests(t)) {
      throw new RuntimeException(t);
    }
    ourCallbackExceptions.get().fire(t);
    t.printStackTrace();
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
}