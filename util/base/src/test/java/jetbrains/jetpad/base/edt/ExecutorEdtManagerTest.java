/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.ThrowableHandlers;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ExecutorEdtManagerTest extends BaseTestCase {
  private boolean mySuccess;

  @Test
  public void testSubmitAfterShutdown() {
    EventDispatchThreadManager tm = new ExecutorEdtManager("My task manager");
    EventDispatchThread edt = tm.getEDT();
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        ThrowableHandlers.addHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable event) {
            throw (event instanceof RuntimeException) ? (RuntimeException) event : new RuntimeException(event);
          }
        });
      }
    });
    tm.finish();
    edt.schedule(new Runnable() {
      @Override
      public void run() {
        mySuccess = true;
      }
    });
    assertFalse(mySuccess);
  }
}
